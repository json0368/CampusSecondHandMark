package com.cshm.campussecondhandmark.module.review.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.review.pojo.vo.ReviewVO;
import com.cshm.campussecondhandmark.module.review.service.TradeReviewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private TradeReviewService tradeReviewService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReviewController reviewController = new ReviewController();
        ReflectionTestUtils.setField(reviewController, "tradeReviewService", tradeReviewService);
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setValidator(noopValidator())
                .build();
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void createReviewShouldUseApiReviewsPath() throws Exception {
        BaseContext.setCurrentId(10L);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":1,\"score\":5,\"content\":\"smooth trade\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(tradeReviewService).createReview(eq(10L), any());
    }

    @Test
    void pageUserReviewsShouldUseApiUsersReviewsPath() throws Exception {
        ReviewVO reviewVO = new ReviewVO();
        reviewVO.setId(100L);
        reviewVO.setReviewerNickname("buyer");
        reviewVO.setScore(5);

        when(tradeReviewService.pageUserReviews(eq(20L), any())).thenReturn(new PageResult<>(1, List.of(reviewVO)));

        mockMvc.perform(get("/api/users/20/reviews")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(100))
                .andExpect(jsonPath("$.data.records[0].reviewerNickname").value("buyer"));

        verify(tradeReviewService).pageUserReviews(eq(20L), any());
    }

    @Test
    void pageOrderReviewsShouldUseApiOrdersReviewsPath() throws Exception {
        ReviewVO reviewVO = new ReviewVO();
        reviewVO.setId(100L);
        reviewVO.setOrderId(1L);
        reviewVO.setScore(4);

        when(tradeReviewService.pageOrderReviews(1L)).thenReturn(new PageResult<>(1, List.of(reviewVO)));

        mockMvc.perform(get("/api/orders/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records[0].orderId").value(1));

        verify(tradeReviewService).pageOrderReviews(1L);
    }

    private Validator noopValidator() {
        return new Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return true;
            }

            @Override
            public void validate(Object target, Errors errors) {
            }
        };
    }
}
