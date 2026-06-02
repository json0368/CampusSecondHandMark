package com.cshm.campussecondhandmark.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.cshm.campussecondhandmark.module.user.service.support.UserAccessValidator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements TradeOrderService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TradeReviewMapper tradeReviewMapper;
    @Autowired
    private UserAccessValidator userAccessValidator;

    // ==================== 公开接口 ====================

    @Override
    @Transactional
    public Long createOrder(Long currentUserId, Long productId) {
        User buyer = userAccessValidator.getNormalUserOrThrow(currentUserId, "买家必须是普通用户", "买家账号已被封禁");
        assertUserAvailable(buyer, "买家");

        Product product = getProductOrThrow(productId);
        if (currentUserId.equals(product.getSellerId())) {
            throw new BaseException("不能购买自己的商品");
        }
        assertProductOrderable(product);

        User seller = userAccessValidator.getNormalUserOrThrow(product.getSellerId(), "卖家必须是普通用户", "卖家账号已被封禁");
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
        userAccessValidator.getNormalUserOrThrow(currentUserId);
        int pageNum = (dto.getPageNum() != null && dto.getPageNum() > 0) ? dto.getPageNum() : 1;
        int pageSize = (dto.getPageSize() != null && dto.getPageSize() > 0) ? Math.min(dto.getPageSize(), MAX_PAGE_SIZE) : 10;
        Page<TradeOrder> page = new Page<>(pageNum, pageSize);
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
        Map<Long, Product> productsById = mapProductsById(extractIds(resultPage.getRecords(), TradeOrder::getProductId));
        Map<Long, User> usersById = mapUsersById(extractParticipantIds(resultPage.getRecords()));
        return new PageResult<>(resultPage.getTotal(),
                resultPage.getRecords().stream()
                        .map(order -> buildOrderSummaryVO(order, productsById, usersById))
                        .toList());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long currentUserId, Long orderId) {
        userAccessValidator.getNormalUserOrThrow(currentUserId);
        TradeOrder order = getOrderOrThrow(orderId);
        assertOrderParticipant(currentUserId, order);
        return buildOrderDetailVO(currentUserId, order);
    }

    @Override
    @Transactional
    public void confirmOrder(Long currentUserId, Long orderId) {
        userAccessValidator.getNormalUserOrThrow(currentUserId);
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
        userAccessValidator.getNormalUserOrThrow(currentUserId);
        TradeOrder order = getOrderOrThrow(orderId);
        assertOrderParticipant(currentUserId, order);
        if (order.getStatus() != TradeOrderStatusEnum.PENDING_CONFIRM
                && order.getStatus() != TradeOrderStatusEnum.IN_TRANSACTION) {
            throw new BaseException("当前订单状态不能取消");
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<TradeOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TradeOrder::getId, order.getId())
                .eq(TradeOrder::getStatus, order.getStatus())
                .set(TradeOrder::getStatus, TradeOrderStatusEnum.CANCELLED)
                .set(TradeOrder::getCancelReason, trimToNull(remark))
                .set(TradeOrder::getCancelledTime, now)
                .set(TradeOrder::getUpdateTime, now);
        if (!update(updateWrapper)) {
            throw new BaseException("取消订单失败");
        }

        LambdaUpdateWrapper<Product> productUpdate = new LambdaUpdateWrapper<>();
        productUpdate.eq(Product::getId, order.getProductId())
                .set(Product::getSaleStatus, ProductSaleStatusEnum.ON_SHELF)
                .set(Product::getUpdateTime, now);
        if (productMapper.update(null, productUpdate) <= 0) {
            throw new BaseException("恢复商品状态失败");
        }
    }

    @Override
    @Transactional
    public void completeOrder(Long currentUserId, Long orderId) {
        userAccessValidator.getNormalUserOrThrow(currentUserId);
        TradeOrder order = getOrderOrThrow(orderId);
        if (!currentUserId.equals(order.getBuyerId())) {
            throw new BaseException("只有买家可以完成订单");
        }
        if (order.getStatus() != TradeOrderStatusEnum.IN_TRANSACTION) {
            throw new BaseException("当前订单状态不能完成");
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<TradeOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TradeOrder::getId, order.getId())
                .eq(TradeOrder::getStatus, TradeOrderStatusEnum.IN_TRANSACTION)
                .set(TradeOrder::getStatus, TradeOrderStatusEnum.COMPLETED)
                .set(TradeOrder::getCompletedTime, now)
                .set(TradeOrder::getUpdateTime, now);
        if (!update(updateWrapper)) {
            throw new BaseException("完成订单失败");
        }

        LambdaUpdateWrapper<Product> productUpdate = new LambdaUpdateWrapper<>();
        productUpdate.eq(Product::getId, order.getProductId())
                .set(Product::getSaleStatus, ProductSaleStatusEnum.SOLD)
                .set(Product::getUpdateTime, now);
        if (productMapper.update(null, productUpdate) <= 0) {
            throw new BaseException("更新商品售出状态失败");
        }
    }

    // ==================== 权限/状态校验 ====================

    private TradeOrder getOrderOrThrow(Long orderId) {
        TradeOrder order = getById(orderId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        return order;
    }

    private Product getProductOrThrow(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BaseException("商品不存在");
        }
        return product;
    }

    private User getUserOrThrow(Long userId) {
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

    // ==================== VO 构建 ====================

    private OrderSummaryVO buildOrderSummaryVO(TradeOrder order, Map<Long, Product> productsById, Map<Long, User> usersById) {
        OrderSummaryVO vo = new OrderSummaryVO();
        BeanUtils.copyProperties(order, vo);

        Product product = productsById.get(order.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductCoverImageUrl(product.getCoverImageUrl());
        }
        User buyer = usersById.get(order.getBuyerId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
        }
        User seller = usersById.get(order.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
        }
        return vo;
    }

    private OrderDetailVO buildOrderDetailVO(Long currentUserId, TradeOrder order) {
        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);

        Map<Long, Product> productsById = mapProductsById(List.of(order.getProductId()));
        Map<Long, User> usersById = mapUsersById(List.of(order.getBuyerId(), order.getSellerId()));

        Product product = productsById.get(order.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductCoverImageUrl(product.getCoverImageUrl());
        }
        User buyer = usersById.get(order.getBuyerId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
        }
        User seller = usersById.get(order.getSellerId());
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

    // ==================== 工具方法 ====================

    private String normalizeIdentityType(String identityType) {
        String value = trimToNull(identityType);
        if (value == null) return "ALL";
        return value.toUpperCase(Locale.ROOT);
    }

    private Map<Long, Product> mapProductsById(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productMapper.selectBatchIds(productIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Product::getId, Function.identity(), (l, r) -> l));
    }

    private Map<Long, User> mapUsersById(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(User::getId, Function.identity(), (l, r) -> l));
    }

    private List<Long> extractParticipantIds(Collection<TradeOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }
        return orders.stream()
                .flatMap(order -> java.util.stream.Stream.of(order.getBuyerId(), order.getSellerId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private <T> List<Long> extractIds(Collection<T> source, Function<T, Long> extractor) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return source.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String generateOrderNo(LocalDateTime now) {
        return "TO" + ORDER_NO_FORMATTER.format(now) + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
