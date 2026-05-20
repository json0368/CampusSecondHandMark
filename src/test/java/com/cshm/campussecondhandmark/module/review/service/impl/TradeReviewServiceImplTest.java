package com.cshm.campussecondhandmark.module.review.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeReviewServiceImplTest {

    @Mock
    private TradeReviewMapper tradeReviewMapper;

    @Mock
    private TradeOrderMapper tradeOrderMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProductMapper productMapper;

    private TradeReviewServiceImpl tradeReviewService;

    @BeforeEach
    void setUp() {
        tradeReviewService = new TradeReviewServiceImpl();
        ReflectionTestUtils.setField(tradeReviewService, "baseMapper", tradeReviewMapper);
        ReflectionTestUtils.setField(tradeReviewService, "tradeOrderMapper", tradeOrderMapper);
        ReflectionTestUtils.setField(tradeReviewService, "userMapper", userMapper);
        ReflectionTestUtils.setField(tradeReviewService, "productMapper", productMapper);
    }

    @Test
    void createReviewShouldSaveBuyerReviewForCompletedOrder() {
        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setOrderId(1L);
        dto.setScore(5);
        dto.setContent("smooth trade");

        when(tradeOrderMapper.selectById(1L)).thenReturn(completedOrder());
        when(tradeReviewMapper.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            TradeReview review = invocation.getArgument(0);
            review.setId(100L);
            return 1;
        }).when(tradeReviewMapper).insert(any(TradeReview.class));

        tradeReviewService.createReview(10L, dto);

        ArgumentCaptor<TradeReview> captor = ArgumentCaptor.forClass(TradeReview.class);
        verify(tradeReviewMapper).insert(captor.capture());
        TradeReview savedReview = captor.getValue();

        assertEquals(1L, savedReview.getOrderId());
        assertEquals(10L, savedReview.getReviewerId());
        assertEquals(20L, savedReview.getRevieweeId());
        assertEquals(5, savedReview.getScore());
        assertEquals("smooth trade", savedReview.getContent());
        assertNotNull(savedReview.getCreateTime());
        assertNotNull(savedReview.getUpdateTime());
    }

    @Test
    void createReviewShouldRejectNonCompletedOrder() {
        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setOrderId(1L);
        dto.setScore(5);

        TradeOrder order = completedOrder();
        order.setStatus(TradeOrderStatusEnum.IN_TRANSACTION);
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        BaseException exception = assertThrows(BaseException.class, () -> tradeReviewService.createReview(10L, dto));

        assertEquals("订单完成后才能评价", exception.getMessage());
        verify(tradeReviewMapper, never()).insert(any(TradeReview.class));
    }

    @Test
    void createReviewShouldRejectDuplicateReviewerForSameOrder() {
        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setOrderId(1L);
        dto.setScore(4);

        when(tradeOrderMapper.selectById(1L)).thenReturn(completedOrder());
        when(tradeReviewMapper.selectCount(any())).thenReturn(1L);

        BaseException exception = assertThrows(BaseException.class, () -> tradeReviewService.createReview(10L, dto));

        assertEquals("该订单已评价", exception.getMessage());
        verify(tradeReviewMapper, never()).insert(any(TradeReview.class));
    }

    @Test
    void pageUserReviewsShouldReturnReviewVOs() {
        TradeReview review = new TradeReview();
        review.setId(100L);
        review.setOrderId(1L);
        review.setReviewerId(10L);
        review.setRevieweeId(20L);
        review.setScore(5);
        review.setContent("smooth trade");
        review.setCreateTime(LocalDateTime.now());

        Page<TradeReview> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(review));

        ReviewQueryDTO dto = new ReviewQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);

        when(tradeReviewMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(tradeOrderMapper.selectBatchIds(any())).thenReturn(List.of(completedOrder()));
        when(productMapper.selectBatchIds(any())).thenReturn(List.of(product()));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(user(10L, "buyer"), user(20L, "seller")));

        PageResult<ReviewVO> result = tradeReviewService.pageUserReviews(20L, dto);

        assertEquals(1, result.getTotal());
        ReviewVO vo = result.getRecords().get(0);
        assertEquals(100L, vo.getId());
        assertEquals(1L, vo.getOrderId());
        assertEquals(10L, vo.getReviewerId());
        assertEquals("buyer", vo.getReviewerNickname());
        assertEquals(20L, vo.getRevieweeId());
        assertEquals("seller", vo.getRevieweeNickname());
        assertEquals(30L, vo.getProductId());
        assertEquals("book", vo.getProductTitle());
        assertEquals(5, vo.getScore());
        assertEquals("smooth trade", vo.getContent());
    }

    @Test
    void pageUserReviewsShouldBatchLoadOrdersProductsAndUsers() {
        TradeReview review = new TradeReview();
        review.setId(100L);
        review.setOrderId(1L);
        review.setReviewerId(10L);
        review.setRevieweeId(20L);
        review.setScore(5);
        review.setCreateTime(LocalDateTime.now());

        Page<TradeReview> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(review));

        ReviewQueryDTO dto = new ReviewQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);

        when(tradeReviewMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(tradeOrderMapper.selectBatchIds(any())).thenReturn(List.of(completedOrder()));
        when(productMapper.selectBatchIds(any())).thenReturn(List.of(product()));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(user(10L, "buyer"), user(20L, "seller")));

        PageResult<ReviewVO> result = tradeReviewService.pageUserReviews(20L, dto);

        ReviewVO vo = result.getRecords().get(0);
        assertEquals("book", vo.getProductTitle());
        assertEquals("buyer", vo.getReviewerNickname());
        assertEquals("seller", vo.getRevieweeNickname());
        verify(tradeOrderMapper).selectBatchIds(any());
        verify(productMapper).selectBatchIds(any());
        verify(userMapper).selectBatchIds(any());
        verify(tradeOrderMapper, never()).selectById(1L);
        verify(productMapper, never()).selectById(30L);
        verify(userMapper, never()).selectById(10L);
        verify(userMapper, never()).selectById(20L);
    }

    @Test
    void pageOrderReviewsShouldBatchLoadOrdersProductsAndUsers() {
        TradeReview review = new TradeReview();
        review.setId(100L);
        review.setOrderId(1L);
        review.setReviewerId(10L);
        review.setRevieweeId(20L);
        review.setScore(5);
        review.setCreateTime(LocalDateTime.now());

        when(tradeReviewMapper.selectList(any())).thenReturn(List.of(review));
        when(tradeOrderMapper.selectBatchIds(any())).thenReturn(List.of(completedOrder()));
        when(productMapper.selectBatchIds(any())).thenReturn(List.of(product()));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(user(10L, "buyer"), user(20L, "seller")));

        PageResult<ReviewVO> result = tradeReviewService.pageOrderReviews(1L);

        ReviewVO vo = result.getRecords().get(0);
        assertEquals("book", vo.getProductTitle());
        assertEquals("buyer", vo.getReviewerNickname());
        assertEquals("seller", vo.getRevieweeNickname());
        verify(tradeOrderMapper).selectBatchIds(any());
        verify(productMapper).selectBatchIds(any());
        verify(userMapper).selectBatchIds(any());
        verify(tradeOrderMapper, never()).selectById(1L);
        verify(productMapper, never()).selectById(30L);
        verify(userMapper, never()).selectById(10L);
        verify(userMapper, never()).selectById(20L);
    }

    private TradeOrder completedOrder() {
        TradeOrder order = new TradeOrder();
        order.setId(1L);
        order.setProductId(30L);
        order.setBuyerId(10L);
        order.setSellerId(20L);
        order.setStatus(TradeOrderStatusEnum.COMPLETED);
        return order;
    }

    private User user(Long id, String nickname) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        return user;
    }

    private Product product() {
        Product product = new Product();
        product.setId(30L);
        product.setTitle("book");
        product.setCoverImageUrl("https://example.com/book.jpg");
        return product;
    }
}
