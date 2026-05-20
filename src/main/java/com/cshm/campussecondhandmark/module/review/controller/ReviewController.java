package com.cshm.campussecondhandmark.module.review.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.common.result.Result;
import com.cshm.campussecondhandmark.module.review.pojo.dto.ReviewCreateDTO;
import com.cshm.campussecondhandmark.module.review.pojo.dto.ReviewQueryDTO;
import com.cshm.campussecondhandmark.module.review.pojo.vo.ReviewVO;
import com.cshm.campussecondhandmark.module.review.service.TradeReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Api(tags = "评价管理")
public class ReviewController {

    @Autowired
    private TradeReviewService tradeReviewService;

    @PostMapping("/api/reviews")
    @ApiOperation(value = "创建订单评价", notes = "买家在订单完成后对卖家进行评价，评价包含评分和文字内容")
    public Result<Void> createReview(@RequestBody ReviewCreateDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("创建订单评价, currentUserId={}, orderId={}", currentUserId, dto == null ? null : dto.getOrderId());
        tradeReviewService.createReview(currentUserId, dto);
        return Result.success();
    }

    @GetMapping("/api/users/{userId}/reviews")
    @ApiOperation(value = "分页查询用户评价", notes = "分页查询用户的评价记录，包含作为买家和卖家的评价")
    public Result<PageResult<ReviewVO>> pageUserReviews(
            @ApiParam(value = "User ID", required = true, example = "20") @PathVariable Long userId,
            ReviewQueryDTO dto) {
        log.info("分页查询用户评价, userId={}", userId);
        return Result.success(tradeReviewService.pageUserReviews(userId, dto));
    }

    @GetMapping("/api/orders/{orderId}/reviews")
    @ApiOperation(value = "分页查询订单评价")
    public Result<PageResult<ReviewVO>> pageOrderReviews(
            @ApiParam(value = "Order ID", required = true, example = "1") @PathVariable Long orderId) {
        log.info("分页查询订单评价, orderId={}", orderId);
        return Result.success(tradeReviewService.pageOrderReviews(orderId));
    }
}
