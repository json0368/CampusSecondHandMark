package com.cshm.campussecondhandmark.module.product.service.impl;

import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import com.cshm.campussecondhandmark.module.product.mapper.ProductMapper;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductConversationOpenVO;
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
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductConversationServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    private ProductConversationServiceImpl productConversationService;

    @BeforeEach
    void setUp() {
        UserAccessValidator userAccessValidator = new UserAccessValidator();
        ReflectionTestUtils.setField(userAccessValidator, "userMapper", userMapper);

        productConversationService = new ProductConversationServiceImpl();
        ReflectionTestUtils.setField(productConversationService, "productMapper", productMapper);
        ReflectionTestUtils.setField(productConversationService, "userAccessValidator", userAccessValidator);
    }

    @Test
    void openConversationShouldReturnBuyerSellerAndProductContext() {
        when(userMapper.selectById(2L)).thenReturn(normalUser(2L, "买家同学"));
        when(productMapper.selectById(10L)).thenReturn(publicProduct(10L, 3L));
        when(userMapper.selectById(3L)).thenReturn(normalUser(3L, "卖家同学"));

        ProductConversationOpenVO result = productConversationService.openConversation(2L, 10L);

        assertEquals(2L, result.getBuyerId());
        assertEquals("买家同学", result.getBuyerNickname());
        assertEquals("https://example.com/avatar/2.png", result.getBuyerAvatarUrl());
        assertEquals(3L, result.getSellerId());
        assertEquals("卖家同学", result.getSellerNickname());
        assertEquals("https://example.com/avatar/3.png", result.getSellerAvatarUrl());
        assertEquals(10L, result.getProductId());
        assertEquals("九成新机械键盘", result.getProductTitle());
        assertEquals("https://example.com/product/10-cover.jpg", result.getProductCoverUrl());
    }

    @Test
    void openConversationShouldRejectOwnProduct() {
        when(userMapper.selectById(2L)).thenReturn(normalUser(2L, "买家同学"));
        when(productMapper.selectById(10L)).thenReturn(publicProduct(10L, 2L));

        BaseException exception = assertThrows(BaseException.class,
                () -> productConversationService.openConversation(2L, 10L));

        assertEquals("不能联系自己的商品", exception.getMessage());
    }

    @Test
    void openConversationShouldRejectInvisibleProduct() {
        when(userMapper.selectById(2L)).thenReturn(normalUser(2L, "买家同学"));
        when(productMapper.selectById(10L)).thenReturn(hiddenProduct(10L, 3L));

        BaseException exception = assertThrows(BaseException.class,
                () -> productConversationService.openConversation(2L, 10L));

        assertEquals("商品不存在或已下架", exception.getMessage());
    }

    @Test
    void openConversationShouldRejectAbnormalSeller() {
        when(userMapper.selectById(2L)).thenReturn(normalUser(2L, "买家同学"));
        when(productMapper.selectById(10L)).thenReturn(publicProduct(10L, 3L));
        when(userMapper.selectById(3L)).thenReturn(bannedUser(3L, "卖家同学"));

        BaseException exception = assertThrows(BaseException.class,
                () -> productConversationService.openConversation(2L, 10L));

        assertEquals("卖家状态异常，暂时无法联系", exception.getMessage());
    }

    private User normalUser(Long userId, String nickname) {
        User user = new User();
        user.setId(userId);
        user.setNickname(nickname);
        user.setAvatarUrl("https://example.com/avatar/" + userId + ".png");
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.NORMAL);
        return user;
    }

    private User bannedUser(Long userId, String nickname) {
        User user = normalUser(userId, nickname);
        user.setStatus(UserStatusEnum.BANNED);
        return user;
    }

    private Product publicProduct(Long productId, Long sellerId) {
        Product product = new Product();
        product.setId(productId);
        product.setSellerId(sellerId);
        product.setTitle("九成新机械键盘");
        product.setCoverImageUrl("https://example.com/product/10-cover.jpg");
        product.setAuditStatus(ProductAuditStatusEnum.APPROVED);
        product.setSaleStatus(ProductSaleStatusEnum.ON_SHELF);
        return product;
    }

    private Product hiddenProduct(Long productId, Long sellerId) {
        Product product = publicProduct(productId, sellerId);
        product.setSaleStatus(ProductSaleStatusEnum.OFF_SHELF);
        return product;
    }
}
