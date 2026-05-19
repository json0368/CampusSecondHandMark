package com.cshm.campussecondhandmark.module.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.review.pojo.dto.ReviewCreateDTO;
import com.cshm.campussecondhandmark.module.review.pojo.dto.ReviewQueryDTO;
import com.cshm.campussecondhandmark.module.review.pojo.entity.TradeReview;
import com.cshm.campussecondhandmark.module.review.pojo.vo.ReviewVO;

public interface TradeReviewService extends IService<TradeReview> {

    void createReview(Long currentUserId, ReviewCreateDTO dto);

    PageResult<ReviewVO> pageUserReviews(Long userId, ReviewQueryDTO dto);

    PageResult<ReviewVO> pageOrderReviews(Long orderId);

    void assertReviewAllowed(Long currentUserId, Long orderId);
}
