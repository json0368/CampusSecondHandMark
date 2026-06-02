package com.cshm.campussecondhandmark.module.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.order.enums.TradeOrderStatusEnum;
import com.cshm.campussecondhandmark.module.order.mapper.TradeOrderMapper;
import com.cshm.campussecondhandmark.module.order.pojo.entity.TradeOrder;
import com.cshm.campussecondhandmark.module.product.mapper.ProductMapper;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.review.mapper.TradeReviewMapper;
import com.cshm.campussecondhandmark.module.review.pojo.dto.ReviewCreateDTO;
import com.cshm.campussecondhandmark.module.review.pojo.dto.ReviewQueryDTO;
import com.cshm.campussecondhandmark.module.review.pojo.entity.TradeReview;
import com.cshm.campussecondhandmark.module.review.pojo.vo.ReviewVO;
import com.cshm.campussecondhandmark.module.review.service.TradeReviewService;
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import com.cshm.campussecondhandmark.module.user.service.support.UserAccessValidator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TradeReviewServiceImpl extends ServiceImpl<TradeReviewMapper, TradeReview> implements TradeReviewService {

    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private TradeOrderMapper tradeOrderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserAccessValidator userAccessValidator;

    // ==================== 公开接口 ====================

    @Override
    @Transactional
    public void createReview(Long currentUserId, ReviewCreateDTO dto) {
        userAccessValidator.getNormalUserOrThrow(currentUserId);
        TradeOrder order = getOrderOrThrow(dto.getOrderId());
        assertOrderParticipant(currentUserId, order);
        if (order.getStatus() != TradeOrderStatusEnum.COMPLETED) {
            throw new BaseException("订单完成后才能评价");
        }
        assertNoDuplicateReview(currentUserId, order.getId());

        LocalDateTime now = LocalDateTime.now();
        TradeReview review = new TradeReview();
        review.setOrderId(order.getId());
        review.setReviewerId(currentUserId);
        review.setRevieweeId(resolveRevieweeId(currentUserId, order));
        review.setScore(dto.getScore());
        review.setContent(trimToNull(dto.getContent()));
        review.setCreateTime(now);
        review.setUpdateTime(now);
        if (!save(review)) {
            throw new BaseException("评价失败");
        }
    }

    @Override
    public PageResult<ReviewVO> pageUserReviews(Long userId, ReviewQueryDTO dto) {
        Page<TradeReview> page = new Page<>(resolvePageNum(dto), resolvePageSize(dto));
        LambdaQueryWrapper<TradeReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeReview::getRevieweeId, userId)
                .orderByDesc(TradeReview::getCreateTime)
                .orderByDesc(TradeReview::getId);

        Page<TradeReview> resultPage = page(page, queryWrapper);
        return buildReviewPageResult(resultPage.getTotal(), resultPage.getRecords());
    }

    @Override
    public PageResult<ReviewVO> pageOrderReviews(Long orderId) {
        LambdaQueryWrapper<TradeReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeReview::getOrderId, orderId)
                .orderByDesc(TradeReview::getCreateTime)
                .orderByDesc(TradeReview::getId);
        List<TradeReview> reviews = list(queryWrapper);
        return buildReviewPageResult(reviews.size(), reviews);
    }

    @Override
    public void assertReviewAllowed(Long currentUserId, Long orderId) {
        userAccessValidator.getNormalUserOrThrow(currentUserId);
        TradeOrder order = getOrderOrThrow(orderId);
        assertOrderParticipant(currentUserId, order);
        if (order.getStatus() != TradeOrderStatusEnum.COMPLETED) {
            throw new BaseException("订单完成后才能评价");
        }
        assertNoDuplicateReview(currentUserId, order.getId());
    }

    // ==================== 权限/状态校验 ====================

    private TradeOrder getOrderOrThrow(Long orderId) {
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        return order;
    }

    private void assertOrderParticipant(Long currentUserId, TradeOrder order) {
        if (!currentUserId.equals(order.getBuyerId()) && !currentUserId.equals(order.getSellerId())) {
            throw new BaseException("无权评价该订单");
        }
    }

    private void assertNoDuplicateReview(Long reviewerId, Long orderId) {
        LambdaQueryWrapper<TradeReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeReview::getOrderId, orderId)
                .eq(TradeReview::getReviewerId, reviewerId);
        if (count(queryWrapper) > 0) {
            throw new BaseException("该订单已评价");
        }
    }

    // ==================== VO 构建 ====================

    private ReviewVO buildReviewVO(TradeReview review,
                                   Map<Long, TradeOrder> ordersById,
                                   Map<Long, Product> productsById,
                                   Map<Long, User> usersById) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);

        TradeOrder order = ordersById.get(review.getOrderId());
        if (order != null) {
            vo.setProductId(order.getProductId());
            Product product = productsById.get(order.getProductId());
            if (product != null) {
                vo.setProductTitle(product.getTitle());
                vo.setProductCoverImageUrl(product.getCoverImageUrl());
            }
        }
        User reviewer = usersById.get(review.getReviewerId());
        if (reviewer != null) {
            vo.setReviewerNickname(reviewer.getNickname());
            vo.setReviewerAvatarUrl(reviewer.getAvatarUrl());
        }
        User reviewee = usersById.get(review.getRevieweeId());
        if (reviewee != null) {
            vo.setRevieweeNickname(reviewee.getNickname());
            vo.setRevieweeAvatarUrl(reviewee.getAvatarUrl());
        }
        return vo;
    }

    private PageResult<ReviewVO> buildReviewPageResult(long total, List<TradeReview> reviews) {
        Map<Long, TradeOrder> ordersById = mapOrdersById(extractIds(reviews, TradeReview::getOrderId));
        Map<Long, Product> productsById = mapProductsById(extractIds(ordersById.values(), TradeOrder::getProductId));
        Map<Long, User> usersById = mapUsersById(extractUserIds(reviews));
        return new PageResult<>(total, reviews.stream()
                .map(review -> buildReviewVO(review, ordersById, productsById, usersById))
                .toList());
    }

    // ==================== 工具方法 ====================

    private Long resolveRevieweeId(Long reviewerId, TradeOrder order) {
        return reviewerId.equals(order.getBuyerId()) ? order.getSellerId() : order.getBuyerId();
    }

    private long resolvePageNum(ReviewQueryDTO dto) {
        return dto.getPageNum() == null || dto.getPageNum() < 1 ? 1 : dto.getPageNum();
    }

    private long resolvePageSize(ReviewQueryDTO dto) {
        return dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : Math.min(dto.getPageSize(), MAX_PAGE_SIZE);
    }

    private Map<Long, TradeOrder> mapOrdersById(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Map.of();
        }
        return tradeOrderMapper.selectBatchIds(orderIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(TradeOrder::getId, Function.identity(), (l, r) -> l));
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

    private List<Long> extractUserIds(Collection<TradeReview> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return List.of();
        }
        return reviews.stream()
                .flatMap(review -> java.util.stream.Stream.of(review.getReviewerId(), review.getRevieweeId()))
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

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
