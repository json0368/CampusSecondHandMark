package com.cshm.campussecondhandmark.module.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.order.enums.TradeOrderStatusEnum;
import com.cshm.campussecondhandmark.module.order.mapper.TradeOrderMapper;
import com.cshm.campussecondhandmark.module.order.pojo.dto.OrderQueryDTO;
import com.cshm.campussecondhandmark.module.order.pojo.entity.TradeOrder;
import com.cshm.campussecondhandmark.module.order.pojo.vo.OrderDetailVO;
import com.cshm.campussecondhandmark.module.order.pojo.vo.OrderSummaryVO;
import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import com.cshm.campussecondhandmark.module.product.mapper.ProductMapper;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.review.mapper.TradeReviewMapper;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import com.cshm.campussecondhandmark.module.user.service.support.UserAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        UserAccessValidator userAccessValidator = new UserAccessValidator();
        ReflectionTestUtils.setField(userAccessValidator, "userMapper", userMapper);
        tradeOrderService = new TradeOrderServiceImpl();
        ReflectionTestUtils.setField(tradeOrderService, "baseMapper", tradeOrderMapper);
        ReflectionTestUtils.setField(tradeOrderService, "productMapper", productMapper);
        ReflectionTestUtils.setField(tradeOrderService, "userMapper", userMapper);
        ReflectionTestUtils.setField(tradeOrderService, "tradeReviewMapper", tradeReviewMapper);
        ReflectionTestUtils.setField(tradeOrderService, "userAccessValidator", userAccessValidator);
    }

    private User bannedUser(Long id, String nickname) {
        User user = user(id, nickname);
        user.setStatus(UserStatusEnum.BANNED);
        return user;
    }

    @Test
    void getOrderDetailShouldDisableReviewWhenCurrentUserAlreadyReviewed() {
        when(userMapper.selectById(10L)).thenReturn(user(10L, "buyer"));
        when(tradeOrderMapper.selectById(1L)).thenReturn(completedOrder());
        when(productMapper.selectById(30L)).thenReturn(product());
        when(userMapper.selectById(10L)).thenReturn(user(10L, "buyer"));
        when(userMapper.selectById(20L)).thenReturn(user(20L, "seller"));
        when(tradeReviewMapper.selectCount(any())).thenReturn(1L);

        OrderDetailVO detailVO = tradeOrderService.getOrderDetail(10L, 1L);

        assertFalse(detailVO.getCanReview());
    }

    @Test
    void getOrderDetailShouldRejectBannedCurrentUser() {
        when(userMapper.selectById(10L)).thenReturn(bannedUser(10L, "buyer"));
        when(tradeOrderMapper.selectById(1L)).thenReturn(completedOrder());
        when(productMapper.selectBatchIds(any())).thenReturn(List.of(product()));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(user(10L, "buyer"), user(20L, "seller")));

        BaseException exception = assertThrows(BaseException.class, () -> tradeOrderService.getOrderDetail(10L, 1L));

        assertEquals("账号已被封禁", exception.getMessage());
    }

    @Test
    void confirmOrderShouldRejectBannedSeller() {
        TradeOrder order = completedOrder();
        order.setStatus(TradeOrderStatusEnum.PENDING_CONFIRM);

        when(userMapper.selectById(20L)).thenReturn(bannedUser(20L, "seller"));
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        when(tradeOrderMapper.updateById(any(TradeOrder.class))).thenReturn(1);

        BaseException exception = assertThrows(BaseException.class, () -> tradeOrderService.confirmOrder(20L, 1L));

        assertEquals("账号已被封禁", exception.getMessage());
    }

    @Test
    void pageMyOrdersShouldBatchLoadRelatedProductAndUsers() {
        Page<TradeOrder> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(completedOrder()));

        OrderQueryDTO dto = new OrderQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);

        when(userMapper.selectById(10L)).thenReturn(user(10L, "buyer"));
        when(tradeOrderMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(productMapper.selectBatchIds(any())).thenReturn(List.of(product()));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(user(10L, "buyer"), user(20L, "seller")));

        PageResult<OrderSummaryVO> result = tradeOrderService.pageMyOrders(10L, dto);

        OrderSummaryVO vo = result.getRecords().get(0);
        org.junit.jupiter.api.Assertions.assertEquals("book", vo.getProductTitle());
        org.junit.jupiter.api.Assertions.assertEquals("buyer", vo.getBuyerNickname());
        org.junit.jupiter.api.Assertions.assertEquals("seller", vo.getSellerNickname());
        verify(productMapper).selectBatchIds(any());
        verify(userMapper).selectBatchIds(any());
        verify(productMapper, never()).selectById(30L);
        verify(userMapper).selectById(10L);
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

    private Product product() {
        Product product = new Product();
        product.setId(30L);
        product.setTitle("book");
        product.setAuditStatus(ProductAuditStatusEnum.APPROVED);
        product.setSaleStatus(ProductSaleStatusEnum.ON_SHELF);
        return product;
    }

    private User user(Long id, String nickname) {
        User user = new User();
        user.setId(id);
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.NORMAL);
        user.setNickname(nickname);
        return user;
    }
}
