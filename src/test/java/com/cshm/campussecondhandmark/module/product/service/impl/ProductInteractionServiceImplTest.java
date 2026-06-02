package com.cshm.campussecondhandmark.module.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.product.enums.CategoryStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductConditionEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import com.cshm.campussecondhandmark.module.product.mapper.ProductBrowseHistoryMapper;
import com.cshm.campussecondhandmark.module.product.mapper.ProductCategoryMapper;
import com.cshm.campussecondhandmark.module.product.mapper.ProductFavoriteMapper;
import com.cshm.campussecondhandmark.module.product.mapper.ProductMapper;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductQueryDTO;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductBrowseHistory;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductCategory;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductFavorite;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductSummaryVO;
import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import com.cshm.campussecondhandmark.module.user.service.support.UserAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductInteractionServiceImplTest {

    @Mock
    private ProductFavoriteMapper productFavoriteMapper;

    @Mock
    private ProductBrowseHistoryMapper productBrowseHistoryMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductCategoryMapper productCategoryMapper;

    @Mock
    private UserMapper userMapper;

    private ProductInteractionServiceImpl productInteractionService;

    @BeforeEach
    void setUp() {
        UserAccessValidator userAccessValidator = new UserAccessValidator();
        ReflectionTestUtils.setField(userAccessValidator, "userMapper", userMapper);
        productInteractionService = new ProductInteractionServiceImpl();
        ReflectionTestUtils.setField(productInteractionService, "productFavoriteMapper", productFavoriteMapper);
        ReflectionTestUtils.setField(productInteractionService, "productBrowseHistoryMapper", productBrowseHistoryMapper);
        ReflectionTestUtils.setField(productInteractionService, "productMapper", productMapper);
        ReflectionTestUtils.setField(productInteractionService, "productCategoryMapper", productCategoryMapper);
        ReflectionTestUtils.setField(productInteractionService, "userMapper", userMapper);
        ReflectionTestUtils.setField(productInteractionService, "userAccessValidator", userAccessValidator);
    }

    private User normalUser(Long userId, String nickname) {
        User user = new User();
        user.setId(userId);
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.NORMAL);
        user.setNickname(nickname);
        return user;
    }

    private User bannedUser(Long userId, String nickname) {
        User user = normalUser(userId, nickname);
        user.setStatus(UserStatusEnum.BANNED);
        return user;
    }

    @Test
    void favoriteProductShouldInsertFavoriteRecord() {
        when(userMapper.selectById(3L)).thenReturn(normalUser(3L, "李四"));
        when(productMapper.selectById(10L)).thenReturn(approvedOnShelfProduct());
        when(productFavoriteMapper.selectCount(any())).thenReturn(0L);
        when(productFavoriteMapper.insert(any(ProductFavorite.class))).thenReturn(1);

        productInteractionService.favoriteProduct(3L, 10L);

        ArgumentCaptor<ProductFavorite> captor = ArgumentCaptor.forClass(ProductFavorite.class);
        verify(productFavoriteMapper).insert(captor.capture());
        ProductFavorite favorite = captor.getValue();
        assertEquals(3L, favorite.getUserId());
        assertEquals(10L, favorite.getProductId());
        assertNotNull(favorite.getCreateTime());
        assertNotNull(favorite.getUpdateTime());
    }

    @Test
    void favoriteProductShouldRejectBannedUser() {
        when(userMapper.selectById(3L)).thenReturn(bannedUser(3L, "李四"));
        when(productMapper.selectById(10L)).thenReturn(approvedOnShelfProduct());
        when(productFavoriteMapper.selectCount(any())).thenReturn(0L);
        when(productFavoriteMapper.insert(any(ProductFavorite.class))).thenReturn(1);

        BaseException exception = assertThrows(BaseException.class,
                () -> productInteractionService.favoriteProduct(3L, 10L));

        assertEquals("账号已被封禁", exception.getMessage());
        verify(productFavoriteMapper, never()).insert(any(ProductFavorite.class));
    }

    @Test
    void favoriteProductShouldBeIdempotentWhenAlreadyFavorited() {
        when(userMapper.selectById(3L)).thenReturn(normalUser(3L, "李四"));
        when(productMapper.selectById(10L)).thenReturn(approvedOnShelfProduct());
        when(productFavoriteMapper.selectCount(any())).thenReturn(1L);

        productInteractionService.favoriteProduct(3L, 10L);

        verify(productFavoriteMapper, never()).insert(any(ProductFavorite.class));
    }

    @Test
    void favoriteProductShouldRejectSelfProduct() {
        when(userMapper.selectById(2L)).thenReturn(normalUser(2L, "张三"));
        when(productMapper.selectById(10L)).thenReturn(approvedOnShelfProduct());

        BaseException exception = assertThrows(BaseException.class,
                () -> productInteractionService.favoriteProduct(2L, 10L));

        assertEquals("不能收藏自己的商品", exception.getMessage());
        verify(productFavoriteMapper, never()).insert(any(ProductFavorite.class));
    }

    @Test
    void unfavoriteProductShouldDeleteFavoriteRecordIdempotently() {
        when(userMapper.selectById(3L)).thenReturn(normalUser(3L, "李四"));

        productInteractionService.unfavoriteProduct(3L, 10L);

        verify(productFavoriteMapper).delete(any());
    }

    @Test
    void recordBrowseHistoryShouldInsertWhenNoHistoryExists() {
        when(userMapper.selectById(3L)).thenReturn(normalUser(3L, "李四"));
        when(productMapper.selectById(10L)).thenReturn(approvedOnShelfProduct());
        when(productBrowseHistoryMapper.selectOne(any())).thenReturn(null);
        when(productBrowseHistoryMapper.insert(any(ProductBrowseHistory.class))).thenReturn(1);

        productInteractionService.recordBrowseHistory(3L, 10L);

        ArgumentCaptor<ProductBrowseHistory> captor = ArgumentCaptor.forClass(ProductBrowseHistory.class);
        verify(productBrowseHistoryMapper).insert(captor.capture());
        ProductBrowseHistory history = captor.getValue();
        assertEquals(3L, history.getUserId());
        assertEquals(10L, history.getProductId());
        assertNotNull(history.getBrowseTime());
    }

    @Test
    void recordBrowseHistoryShouldUpdateWhenHistoryExists() {
        ProductBrowseHistory history = new ProductBrowseHistory();
        history.setId(100L);
        history.setUserId(3L);
        history.setProductId(10L);
        history.setBrowseTime(LocalDateTime.now().minusDays(1));

        when(userMapper.selectById(3L)).thenReturn(normalUser(3L, "李四"));
        when(productMapper.selectById(10L)).thenReturn(approvedOnShelfProduct());
        when(productBrowseHistoryMapper.selectOne(any())).thenReturn(history);
        when(productBrowseHistoryMapper.updateById(any(ProductBrowseHistory.class))).thenReturn(1);

        productInteractionService.recordBrowseHistory(3L, 10L);

        verify(productBrowseHistoryMapper, never()).insert(any(ProductBrowseHistory.class));
        verify(productBrowseHistoryMapper).updateById(any(ProductBrowseHistory.class));
    }

    @Test
    void recordBrowseHistoryShouldIgnoreSelfProduct() {
        when(userMapper.selectById(2L)).thenReturn(normalUser(2L, "张三"));
        when(productMapper.selectById(10L)).thenReturn(approvedOnShelfProduct());

        productInteractionService.recordBrowseHistory(2L, 10L);

        verify(productBrowseHistoryMapper, never()).insert(any(ProductBrowseHistory.class));
        verify(productBrowseHistoryMapper, never()).updateById(any(ProductBrowseHistory.class));
    }

    @Test
    void pageMyFavoritesShouldReturnProductSummariesByFavoriteTimeDesc() {
        ProductFavorite favorite = new ProductFavorite();
        favorite.setUserId(3L);
        favorite.setProductId(10L);

        when(userMapper.selectById(3L)).thenReturn(normalUser(3L, "李四"));
        when(productFavoriteMapper.selectList(any())).thenReturn(List.of(favorite));
        when(productMapper.selectBatchIds(any())).thenReturn(List.of(approvedOnShelfProduct()));
        when(productCategoryMapper.selectBatchIds(any())).thenReturn(List.of(enabledCategory()));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(seller()));

        PageResult<ProductSummaryVO> result = productInteractionService.pageMyFavorites(3L, new ProductQueryDTO());

        assertEquals(1, result.getTotal());
        assertEquals(10L, result.getRecords().get(0).getId());
        assertEquals("教材图书", result.getRecords().get(0).getCategoryName());
        assertEquals("张三", result.getRecords().get(0).getSellerNickname());
    }

    @Test
    void pageMyFavoritesShouldExcludeInvisibleProductsFromTotal() {
        ProductFavorite visibleFavorite = new ProductFavorite();
        visibleFavorite.setUserId(3L);
        visibleFavorite.setProductId(10L);
        ProductFavorite hiddenFavorite = new ProductFavorite();
        hiddenFavorite.setUserId(3L);
        hiddenFavorite.setProductId(11L);

        Page<ProductFavorite> page = new Page<>(1, 10);
        page.setTotal(2);
        page.setRecords(List.of(visibleFavorite, hiddenFavorite));

        when(userMapper.selectById(3L)).thenReturn(normalUser(3L, "李四"));
        when(productFavoriteMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(productFavoriteMapper.selectList(any())).thenReturn(List.of(visibleFavorite, hiddenFavorite));
        when(productMapper.selectBatchIds(any())).thenReturn(List.of(approvedOnShelfProduct(), offShelfProduct()));
        when(productCategoryMapper.selectBatchIds(any())).thenReturn(List.of(enabledCategory()));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(seller()));

        PageResult<ProductSummaryVO> result = productInteractionService.pageMyFavorites(3L, new ProductQueryDTO());

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(10L, result.getRecords().get(0).getId());
    }

    @Test
    void pageMyBrowseHistoryShouldExcludeInvisibleProductsFromTotal() {
        ProductBrowseHistory visibleHistory = new ProductBrowseHistory();
        visibleHistory.setUserId(3L);
        visibleHistory.setProductId(10L);
        visibleHistory.setBrowseTime(LocalDateTime.now());
        ProductBrowseHistory hiddenHistory = new ProductBrowseHistory();
        hiddenHistory.setUserId(3L);
        hiddenHistory.setProductId(11L);
        hiddenHistory.setBrowseTime(LocalDateTime.now().minusMinutes(1));

        Page<ProductBrowseHistory> page = new Page<>(1, 10);
        page.setTotal(2);
        page.setRecords(List.of(visibleHistory, hiddenHistory));

        when(userMapper.selectById(3L)).thenReturn(normalUser(3L, "李四"));
        when(productBrowseHistoryMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(productBrowseHistoryMapper.selectList(any())).thenReturn(List.of(visibleHistory, hiddenHistory));
        when(productMapper.selectBatchIds(any())).thenReturn(List.of(approvedOnShelfProduct(), offShelfProduct()));
        when(productCategoryMapper.selectBatchIds(any())).thenReturn(List.of(enabledCategory()));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(seller()));

        PageResult<ProductSummaryVO> result = productInteractionService.pageMyBrowseHistory(3L, new ProductQueryDTO());

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(10L, result.getRecords().get(0).getId());
    }

    @Test
    void clearMyBrowseHistoryShouldDeleteCurrentUserHistory() {
        when(userMapper.selectById(3L)).thenReturn(normalUser(3L, "李四"));
        productInteractionService.clearMyBrowseHistory(3L);

        verify(productBrowseHistoryMapper).delete(any());
    }

    @Test
    void isFavoritedShouldReturnTrueWhenFavoriteExists() {
        when(userMapper.selectById(3L)).thenReturn(normalUser(3L, "李四"));
        when(productFavoriteMapper.selectCount(any())).thenReturn(1L);

        assertTrue(productInteractionService.isFavorited(3L, 10L));
    }

    private Product approvedOnShelfProduct() {
        Product product = new Product();
        product.setId(10L);
        product.setSellerId(2L);
        product.setCategoryId(1L);
        product.setTitle("高等数学教材");
        product.setDescription("九成新");
        product.setPrice(new BigDecimal("25.00"));
        product.setConditionLevel(ProductConditionEnum.LIKE_NEW);
        product.setCoverImageUrl("https://example.com/1.jpg");
        product.setAuditStatus(ProductAuditStatusEnum.APPROVED);
        product.setSaleStatus(ProductSaleStatusEnum.ON_SHELF);
        product.setPublishTime(LocalDateTime.now());
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        return product;
    }

    private Product offShelfProduct() {
        Product product = approvedOnShelfProduct();
        product.setId(11L);
        product.setSaleStatus(ProductSaleStatusEnum.OFF_SHELF);
        return product;
    }

    private ProductCategory enabledCategory() {
        ProductCategory category = new ProductCategory();
        category.setId(1L);
        category.setName("教材图书");
        category.setStatus(CategoryStatusEnum.ENABLED);
        return category;
    }

    private User seller() {
        User user = new User();
        user.setId(2L);
        user.setNickname("张三");
        user.setCampusVerifyStatus(CampusVerifyStatusEnum.APPROVED);
        return user;
    }
}
