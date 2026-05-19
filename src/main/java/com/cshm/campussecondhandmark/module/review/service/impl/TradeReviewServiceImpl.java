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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class TradeReviewServiceImpl extends ServiceImpl<TradeReviewMapper, TradeReview> implements TradeReviewService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;
    private static final int MAX_CONTENT_LENGTH = 500;

    @Autowired
    private TradeOrderMapper tradeOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public void createReview(Long currentUserId, ReviewCreateDTO dto) {
        if (dto == null) {
            throw new BaseException("评价参数不能为空");
        }
        validateScore(dto.getScore());

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
        review.setContent(normalizeContent(dto.getContent()));
        review.setCreateTime(now);
        review.setUpdateTime(now);
        if (!save(review)) {
            throw new BaseException("评价失败");
        }
    }

    @Override
    public PageResult<ReviewVO> pageUserReviews(Long userId, ReviewQueryDTO dto) {
        if (userId == null) {
            throw new BaseException("用户不存在");
        }

        Page<TradeReview> page = new Page<>(resolvePageNum(dto), resolvePageSize(dto));
        LambdaQueryWrapper<TradeReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeReview::getRevieweeId, userId)
                .orderByDesc(TradeReview::getCreateTime)
                .orderByDesc(TradeReview::getId);

        Page<TradeReview> resultPage = page(page, queryWrapper);
        return new PageResult<>(resultPage.getTotal(), resultPage.getRecords().stream().map(this::buildReviewVO).toList());
    }

    @Override
    public PageResult<ReviewVO> pageOrderReviews(Long orderId) {
        if (orderId == null) {
            throw new BaseException("订单不存在");
        }

        LambdaQueryWrapper<TradeReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeReview::getOrderId, orderId)
                .orderByDesc(TradeReview::getCreateTime)
                .orderByDesc(TradeReview::getId);
        return new PageResult<>(count(queryWrapper), list(queryWrapper).stream().map(this::buildReviewVO).toList());
    }

    @Override
    public void assertReviewAllowed(Long currentUserId, Long orderId) {
        TradeOrder order = getOrderOrThrow(orderId);
        assertOrderParticipant(currentUserId, order);
        if (order.getStatus() != TradeOrderStatusEnum.COMPLETED) {
            throw new BaseException("订单完成后才能评价");
        }
        assertNoDuplicateReview(currentUserId, order.getId());
    }

    private TradeOrder getOrderOrThrow(Long orderId) {
        if (orderId == null) {
            throw new BaseException("订单不存在");
        }
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        return order;
    }

    private void validateScore(Integer score) {
        if (score == null || score < MIN_SCORE || score > MAX_SCORE) {
            throw new BaseException("评分必须在1到5之间");
        }
    }

    private void assertOrderParticipant(Long currentUserId, TradeOrder order) {
        if (currentUserId == null
                || (!currentUserId.equals(order.getBuyerId()) && !currentUserId.equals(order.getSellerId()))) {
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

    private Long resolveRevieweeId(Long reviewerId, TradeOrder order) {
        return reviewerId.equals(order.getBuyerId()) ? order.getSellerId() : order.getBuyerId();
    }

    private String normalizeContent(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new BaseException("评价内容不能超过500个字符");
        }
        return trimmed;
    }

    private long resolvePageNum(ReviewQueryDTO dto) {
        if (dto == null || dto.getPageNum() == null || dto.getPageNum() < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return dto.getPageNum();
    }

    private long resolvePageSize(ReviewQueryDTO dto) {
        if (dto == null || dto.getPageSize() == null || dto.getPageSize() < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(dto.getPageSize(), MAX_PAGE_SIZE);
    }

    private ReviewVO buildReviewVO(TradeReview review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setReviewerId(review.getReviewerId());
        vo.setRevieweeId(review.getRevieweeId());
        vo.setScore(review.getScore());
        vo.setContent(review.getContent());
        vo.setCreateTime(review.getCreateTime());

        TradeOrder order = tradeOrderMapper.selectById(review.getOrderId());
        if (order != null) {
            vo.setProductId(order.getProductId());
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                vo.setProductTitle(product.getTitle());
                vo.setProductCoverImageUrl(product.getCoverImageUrl());
            }
        }

        User reviewer = userMapper.selectById(review.getReviewerId());
        if (reviewer != null) {
            vo.setReviewerNickname(reviewer.getNickname());
            vo.setReviewerAvatarUrl(reviewer.getAvatarUrl());
        }
        User reviewee = userMapper.selectById(review.getRevieweeId());
        if (reviewee != null) {
            vo.setRevieweeNickname(reviewee.getNickname());
            vo.setRevieweeAvatarUrl(reviewee.getAvatarUrl());
        }
        return vo;
    }
}
