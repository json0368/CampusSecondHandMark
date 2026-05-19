package com.cshm.campussecondhandmark.module.order.service.impl;

import com.cshm.campussecondhandmark.module.order.enums.TradeOrderStatusEnum;
import com.cshm.campussecondhandmark.module.order.mapper.TradeOrderMapper;
import com.cshm.campussecondhandmark.module.order.pojo.entity.TradeOrder;
import com.cshm.campussecondhandmark.module.order.pojo.vo.OrderDetailVO;
import com.cshm.campussecondhandmark.module.product.mapper.ProductMapper;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.review.mapper.TradeReviewMapper;
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeOrderServiceImplTest {

    @Mock
    private TradeOrderMapper tradeOrderMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TradeReviewMapper tradeReviewMapper;

    private TradeOrderServiceImpl tradeOrderService;

    @BeforeEach
    void setUp() {
        tradeOrderService = new TradeOrderServiceImpl();
        ReflectionTestUtils.setField(tradeOrderService, "baseMapper", tradeOrderMapper);
        ReflectionTestUtils.setField(tradeOrderService, "productMapper", productMapper);
        ReflectionTestUtils.setField(tradeOrderService, "userMapper", userMapper);
        ReflectionTestUtils.setField(tradeOrderService, "tradeReviewMapper", tradeReviewMapper);
    }

    @Test
    void getOrderDetailShouldDisableReviewWhenCurrentUserAlreadyReviewed() {
        when(tradeOrderMapper.selectById(1L)).thenReturn(completedOrder());
        when(productMapper.selectById(30L)).thenReturn(product());
        when(userMapper.selectById(10L)).thenReturn(user(10L, "buyer"));
        when(userMapper.selectById(20L)).thenReturn(user(20L, "seller"));
        when(tradeReviewMapper.selectCount(any())).thenReturn(1L);

        OrderDetailVO detailVO = tradeOrderService.getOrderDetail(10L, 1L);

        assertFalse(detailVO.getCanReview());
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

    private Product product() {
        Product product = new Product();
        product.setId(30L);
        product.setTitle("book");
        return product;
    }

    private User user(Long id, String nickname) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        return user;
    }
}
