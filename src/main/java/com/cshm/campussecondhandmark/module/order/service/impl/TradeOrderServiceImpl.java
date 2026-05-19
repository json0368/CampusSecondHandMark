package com.cshm.campussecondhandmark.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.order.enums.TradeOrderStatusEnum;
import com.cshm.campussecondhandmark.module.order.mapper.TradeOrderMapper;
import com.cshm.campussecondhandmark.module.order.pojo.dto.OrderQueryDTO;
import com.cshm.campussecondhandmark.module.order.pojo.entity.TradeOrder;
import com.cshm.campussecondhandmark.module.order.pojo.vo.OrderDetailVO;
import com.cshm.campussecondhandmark.module.order.pojo.vo.OrderSummaryVO;
import com.cshm.campussecondhandmark.module.order.service.TradeOrderService;
import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import com.cshm.campussecondhandmark.module.product.mapper.ProductMapper;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.review.mapper.TradeReviewMapper;
import com.cshm.campussecondhandmark.module.review.pojo.entity.TradeReview;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements TradeOrderService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_REMARK_LENGTH = 255;
    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TradeReviewMapper tradeReviewMapper;

    @Override
    @Transactional
    public Long createOrder(Long currentUserId, Long productId) {
        if (productId == null) {
            throw new BaseException("下单商品不能为空");
        }

        User buyer = getUserOrThrow(currentUserId);
        assertUserAvailable(buyer, "买家");

        Product product = getProductOrThrow(productId);
        if (currentUserId.equals(product.getSellerId())) {
            throw new BaseException("不能购买自己的商品");
        }
        assertProductOrderable(product);

        User seller = getUserOrThrow(product.getSellerId());
        assertUserAvailable(seller, "卖家");
        assertNoActiveOrder(product.getId());

        LocalDateTime now = LocalDateTime.now();
        TradeOrder order = new TradeOrder();
        order.setOrderNo(generateOrderNo(now));
        order.setProductId(product.getId());
        order.setBuyerId(buyer.getId());
        order.setSellerId(seller.getId());
        order.setOrderAmount(product.getPrice());
        order.setStatus(TradeOrderStatusEnum.PENDING_CONFIRM);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        if (!save(order)) {
            throw new BaseException("创建订单失败");
        }

        product.setSaleStatus(ProductSaleStatusEnum.IN_TRANSACTION);
        product.setUpdateTime(now);
        if (productMapper.updateById(product) <= 0) {
            throw new BaseException("更新商品状态失败");
        }
        return order.getId();
    }

    @Override
    public PageResult<OrderSummaryVO> pageMyOrders(Long currentUserId, OrderQueryDTO dto) {
        Page<TradeOrder> page = new Page<>(dto.getPageNum(), Math.min(dto.getPageSize(), MAX_PAGE_SIZE));
        LambdaQueryWrapper<TradeOrder> queryWrapper = new LambdaQueryWrapper<>();

        String identityType = normalizeIdentityType(dto.getIdentityType());
        if ("BUYER".equals(identityType)) {
            queryWrapper.eq(TradeOrder::getBuyerId, currentUserId);
        } else if ("SELLER".equals(identityType)) {
            queryWrapper.eq(TradeOrder::getSellerId, currentUserId);
        } else {
            queryWrapper.and(wrapper -> wrapper.eq(TradeOrder::getBuyerId, currentUserId)
                    .or()
                    .eq(TradeOrder::getSellerId, currentUserId));
        }

        if (dto.getStatus() != null) {
            queryWrapper.eq(TradeOrder::getStatus, dto.getStatus());
        }
        queryWrapper.orderByDesc(TradeOrder::getCreateTime).orderByDesc(TradeOrder::getId);

        Page<TradeOrder> resultPage = page(page, queryWrapper);
        return new PageResult<>(resultPage.getTotal(),
                resultPage.getRecords().stream().map(this::buildOrderSummaryVO).toList());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long currentUserId, Long orderId) {
        TradeOrder order = getOrderOrThrow(orderId);
        assertOrderParticipant(currentUserId, order);
        return buildOrderDetailVO(currentUserId, order);
    }

    @Override
    @Transactional
    public void confirmOrder(Long currentUserId, Long orderId) {
        TradeOrder order = getOrderOrThrow(orderId);
        if (!currentUserId.equals(order.getSellerId())) {
            throw new BaseException("只有卖家可以确认订单");
        }
        if (order.getStatus() != TradeOrderStatusEnum.PENDING_CONFIRM) {
            throw new BaseException("当前订单状态不能确认");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setStatus(TradeOrderStatusEnum.IN_TRANSACTION);
        order.setConfirmedTime(now);
        order.setUpdateTime(now);
        if (!updateById(order)) {
            throw new BaseException("确认订单失败");
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long currentUserId, Long orderId, String remark) {
        TradeOrder order = getOrderOrThrow(orderId);
        assertOrderParticipant(currentUserId, order);
        if (order.getStatus() != TradeOrderStatusEnum.PENDING_CONFIRM
                && order.getStatus() != TradeOrderStatusEnum.IN_TRANSACTION) {
            throw new BaseException("当前订单状态不能取消");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setStatus(TradeOrderStatusEnum.CANCELLED);
        order.setCancelReason(normalizeRemark(remark));
        order.setCancelledTime(now);
        order.setUpdateTime(now);
        if (!updateById(order)) {
            throw new BaseException("取消订单失败");
        }

        Product product = getProductOrThrow(order.getProductId());
        product.setSaleStatus(ProductSaleStatusEnum.ON_SHELF);
        product.setUpdateTime(now);
        if (productMapper.updateById(product) <= 0) {
            throw new BaseException("恢复商品状态失败");
        }
    }

    @Override
    @Transactional
    public void completeOrder(Long currentUserId, Long orderId) {
        TradeOrder order = getOrderOrThrow(orderId);
        if (!currentUserId.equals(order.getBuyerId())) {
            throw new BaseException("只有买家可以完成订单");
        }
        if (order.getStatus() != TradeOrderStatusEnum.IN_TRANSACTION) {
            throw new BaseException("当前订单状态不能完成");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setStatus(TradeOrderStatusEnum.COMPLETED);
        order.setCompletedTime(now);
        order.setUpdateTime(now);
        if (!updateById(order)) {
            throw new BaseException("完成订单失败");
        }

        Product product = getProductOrThrow(order.getProductId());
        product.setSaleStatus(ProductSaleStatusEnum.SOLD);
        product.setUpdateTime(now);
        if (productMapper.updateById(product) <= 0) {
            throw new BaseException("更新商品售出状态失败");
        }
    }

    private TradeOrder getOrderOrThrow(Long orderId) {
        if (orderId == null) {
            throw new BaseException("订单不存在");
        }
        TradeOrder order = getById(orderId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        return order;
    }

    private Product getProductOrThrow(Long productId) {
        if (productId == null) {
            throw new BaseException("商品不存在");
        }
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BaseException("商品不存在");
        }
        return product;
    }

    private User getUserOrThrow(Long userId) {
        if (userId == null) {
            throw new BaseException("用户不存在");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        return user;
    }

    private void assertUserAvailable(User user, String identity) {
        if (user.getStatus() == UserStatusEnum.BANNED) {
            throw new BaseException(identity + "账号已被封禁");
        }
    }

    private void assertProductOrderable(Product product) {
        if (product.getAuditStatus() != ProductAuditStatusEnum.APPROVED) {
            throw new BaseException("商品未审核通过，不能下单");
        }
        if (product.getSaleStatus() != ProductSaleStatusEnum.ON_SHELF) {
            throw new BaseException("商品当前不可下单");
        }
    }

    private void assertNoActiveOrder(Long productId) {
        LambdaQueryWrapper<TradeOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeOrder::getProductId, productId)
                .in(TradeOrder::getStatus, TradeOrderStatusEnum.PENDING_CONFIRM, TradeOrderStatusEnum.IN_TRANSACTION);
        if (count(queryWrapper) > 0) {
            throw new BaseException("该商品当前已有进行中的订单");
        }
    }

    private void assertOrderParticipant(Long currentUserId, TradeOrder order) {
        if (!currentUserId.equals(order.getBuyerId()) && !currentUserId.equals(order.getSellerId())) {
            throw new BaseException("无权查看该订单");
        }
    }

    private String normalizeIdentityType(String identityType) {
        String value = trimToNull(identityType);
        if (!StringUtils.hasText(value)) {
            return "ALL";
        }
        String normalized = value.toUpperCase(Locale.ROOT);
        if (!"BUYER".equals(normalized) && !"SELLER".equals(normalized) && !"ALL".equals(normalized)) {
            throw new BaseException("订单身份类型不正确");
        }
        return normalized;
    }

    private String normalizeRemark(String remark) {
        String value = trimToNull(remark);
        if (value != null && value.length() > MAX_REMARK_LENGTH) {
            throw new BaseException("备注不能超过255个字符");
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String generateOrderNo(LocalDateTime now) {
        return "TO" + ORDER_NO_FORMATTER.format(now) + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private OrderSummaryVO buildOrderSummaryVO(TradeOrder order) {
        OrderSummaryVO vo = new OrderSummaryVO();
        BeanUtils.copyProperties(order, vo);

        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductCoverImageUrl(product.getCoverImageUrl());
        }

        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
        }
        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
        }
        return vo;
    }

    private OrderDetailVO buildOrderDetailVO(Long currentUserId, TradeOrder order) {
        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);

        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductCoverImageUrl(product.getCoverImageUrl());
        }

        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
        }
        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
        }

        boolean currentBuyer = currentUserId.equals(order.getBuyerId());
        boolean currentSeller = currentUserId.equals(order.getSellerId());
        vo.setCanConfirm(currentSeller && order.getStatus() == TradeOrderStatusEnum.PENDING_CONFIRM);
        vo.setCanCancel((currentBuyer || currentSeller)
                && (order.getStatus() == TradeOrderStatusEnum.PENDING_CONFIRM
                || order.getStatus() == TradeOrderStatusEnum.IN_TRANSACTION));
        vo.setCanComplete(currentBuyer && order.getStatus() == TradeOrderStatusEnum.IN_TRANSACTION);
        vo.setCanReview((currentBuyer || currentSeller)
                && order.getStatus() == TradeOrderStatusEnum.COMPLETED
                && !hasReviewed(currentUserId, order.getId()));
        return vo;
    }

    private boolean hasReviewed(Long currentUserId, Long orderId) {
        LambdaQueryWrapper<TradeReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeReview::getOrderId, orderId)
                .eq(TradeReview::getReviewerId, currentUserId);
        return tradeReviewMapper.selectCount(queryWrapper) > 0;
    }
}
