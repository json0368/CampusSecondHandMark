package com.cshm.campussecondhandmark.module.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.product.enums.CategoryStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductAuditStatusEnum;
import com.cshm.campussecondhandmark.module.product.enums.ProductSaleStatusEnum;
import com.cshm.campussecondhandmark.module.product.mapper.ProductCategoryMapper;
import com.cshm.campussecondhandmark.module.product.mapper.ProductImageMapper;
import com.cshm.campussecondhandmark.module.product.mapper.ProductMapper;
import com.cshm.campussecondhandmark.module.product.pojo.dto.AdminProductRemoveDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductAuditDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductAuditQueryDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductCreateDTO;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ProductSearchDTO;
import com.cshm.campussecondhandmark.module.product.pojo.entity.Product;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductCategory;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductImage;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductAuditVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductDetailVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductSummaryVO;
import com.cshm.campussecondhandmark.module.product.service.ProductInteractionService;
import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import com.cshm.campussecondhandmark.module.user.mapper.UserMapper;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductImageMapper productImageMapper;

    @Mock
    private ProductCategoryMapper productCategoryMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProductInteractionService productInteractionService;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl();
        ReflectionTestUtils.setField(productService, "baseMapper", productMapper);
        ReflectionTestUtils.setField(productService, "productImageMapper", productImageMapper);
        ReflectionTestUtils.setField(productService, "productCategoryMapper", productCategoryMapper);
        ReflectionTestUtils.setField(productService, "userMapper", userMapper);
        ReflectionTestUtils.setField(productService, "productInteractionService", productInteractionService);
    }

    @Test
    void createProductShouldSavePendingDraftProductAndImages() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setCategoryId(1L);
        dto.setTitle("  高等数学教材  ");
        dto.setDescription("九成新");
        dto.setPrice(new BigDecimal("25.00"));
        dto.setConditionLevel(1);
        dto.setImageUrls(List.of("https://example.com/1.jpg", "https://example.com/2.jpg"));

        when(productCategoryMapper.selectById(1L)).thenReturn(enabledCategory());
        doAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(100L);
            return 1;
        }).when(productMapper).insert(any(Product.class));
        when(productImageMapper.insert(any(ProductImage.class))).thenReturn(1);

        Long productId = productService.createProduct(2L, dto);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).insert(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertEquals(100L, productId);
        assertEquals(2L, savedProduct.getSellerId());
        assertEquals(1L, savedProduct.getCategoryId());
        assertEquals("高等数学教材", savedProduct.getTitle());
        assertEquals(new BigDecimal("25.00"), savedProduct.getPrice());
        assertEquals(1, savedProduct.getConditionLevel().getCode());
        assertEquals("https://example.com/1.jpg", savedProduct.getCoverImageUrl());
        assertEquals(ProductAuditStatusEnum.PENDING, savedProduct.getAuditStatus());
        assertEquals(ProductSaleStatusEnum.DRAFT, savedProduct.getSaleStatus());
        assertNotNull(savedProduct.getCreateTime());
        assertNotNull(savedProduct.getUpdateTime());

        ArgumentCaptor<ProductImage> imageCaptor = ArgumentCaptor.forClass(ProductImage.class);
        verify(productImageMapper, org.mockito.Mockito.times(2)).insert(imageCaptor.capture());
        List<ProductImage> images = imageCaptor.getAllValues();
        assertEquals(100L, images.get(0).getProductId());
        assertEquals("https://example.com/1.jpg", images.get(0).getImageUrl());
        assertEquals(1, images.get(0).getSortOrder());
        assertEquals("https://example.com/2.jpg", images.get(1).getImageUrl());
        assertEquals(2, images.get(1).getSortOrder());
    }

    @Test
    void pageProductsShouldReturnApprovedOnShelfProductSummary() {
        Product product = approvedOnShelfProduct();
        Page<Product> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(product));

        when(productMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(productCategoryMapper.selectBatchIds(any())).thenReturn(List.of(enabledCategory()));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(seller()));

        PageResult<ProductSummaryVO> result = productService.pageProducts(new ProductSearchDTO());

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        ProductSummaryVO vo = result.getRecords().get(0);
        assertEquals(10L, vo.getId());
        assertEquals("高等数学教材", vo.getTitle());
        assertEquals("教材图书", vo.getCategoryName());
        assertEquals("张三", vo.getSellerNickname());
        assertEquals(CampusVerifyStatusEnum.APPROVED, vo.getSellerCampusVerifyStatus());
        verify(productCategoryMapper).selectBatchIds(any());
        verify(userMapper).selectBatchIds(any());
        verify(productCategoryMapper, never()).selectById(1L);
        verify(userMapper, never()).selectById(2L);
    }

    @Test
    void pageAuditProductsShouldBatchLoadCategoryAndSeller() {
        Product product = approvedOnShelfProduct();
        product.setAuditStatus(ProductAuditStatusEnum.PENDING);
        Page<Product> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(product));

        when(productMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(productCategoryMapper.selectBatchIds(any())).thenReturn(List.of(enabledCategory()));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(seller()));

        PageResult<ProductAuditVO> result = productService.pageAuditProducts(new ProductAuditQueryDTO());

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        ProductAuditVO vo = result.getRecords().get(0);
        assertEquals(10L, vo.getId());
        assertEquals("教材图书", vo.getCategoryName());
        assertEquals("张三", vo.getSellerNickname());
        verify(productCategoryMapper).selectBatchIds(any());
        verify(userMapper).selectBatchIds(any());
        verify(productCategoryMapper, never()).selectById(1L);
        verify(userMapper, never()).selectById(2L);
    }

    @Test
    void getProductDetailShouldHideUnavailableProductFromOtherUsersButAllowSeller() {
        Product product = approvedOnShelfProduct();
        product.setAuditStatus(ProductAuditStatusEnum.PENDING);
        product.setSaleStatus(ProductSaleStatusEnum.DRAFT);

        when(productMapper.selectById(10L)).thenReturn(product);
        when(productCategoryMapper.selectById(1L)).thenReturn(enabledCategory());
        when(userMapper.selectById(2L)).thenReturn(seller());
        when(productImageMapper.selectList(any())).thenReturn(List.of(productImage()));

        BaseException exception = assertThrows(BaseException.class, () -> productService.getProductDetail(10L, 3L));
        assertEquals("商品不存在或未上架", exception.getMessage());

        ProductDetailVO sellerView = productService.getProductDetail(10L, 2L);

        assertEquals(10L, sellerView.getId());
        assertEquals(1, sellerView.getConditionLevel());
        assertEquals(1, sellerView.getImages().size());
        assertFalse(sellerView.getCanOrder());
        assertFalse(sellerView.getCanChat());
    }

    @Test
    void getProductDetailShouldMarkFavoriteAndRecordBrowseHistoryForOtherUser() {
        Product product = approvedOnShelfProduct();

        when(productMapper.selectById(10L)).thenReturn(product);
        when(productCategoryMapper.selectById(1L)).thenReturn(enabledCategory());
        when(userMapper.selectById(2L)).thenReturn(seller());
        when(productImageMapper.selectList(any())).thenReturn(List.of(productImage()));
        when(productInteractionService.isFavorited(3L, 10L)).thenReturn(true);

        ProductDetailVO detail = productService.getProductDetail(10L, 3L);

        assertTrue(detail.getFavorited());
        verify(productInteractionService).recordBrowseHistory(3L, 10L);
    }

    @Test
    void getProductDetailShouldNotRecordBrowseHistoryForSelfProduct() {
        Product product = approvedOnShelfProduct();

        when(productMapper.selectById(10L)).thenReturn(product);
        when(productCategoryMapper.selectById(1L)).thenReturn(enabledCategory());
        when(userMapper.selectById(2L)).thenReturn(seller());
        when(productImageMapper.selectList(any())).thenReturn(List.of(productImage()));

        ProductDetailVO detail = productService.getProductDetail(10L, 2L);

        assertFalse(detail.getFavorited());
        verify(productInteractionService, never()).recordBrowseHistory(2L, 10L);
    }

    @Test
    void auditProductShouldApproveAndPublishProduct() {
        Product product = approvedOnShelfProduct();
        product.setAuditStatus(ProductAuditStatusEnum.PENDING);
        product.setSaleStatus(ProductSaleStatusEnum.DRAFT);
        product.setPublishTime(null);
        product.setAuditTime(null);

        ProductAuditDTO dto = new ProductAuditDTO();
        dto.setAuditStatus(ProductAuditStatusEnum.APPROVED);

        when(productMapper.selectById(10L)).thenReturn(product);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        productService.auditProduct(10L, dto);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).updateById(captor.capture());
        Product updatedProduct = captor.getValue();

        assertEquals(ProductAuditStatusEnum.APPROVED, updatedProduct.getAuditStatus());
        assertEquals(ProductSaleStatusEnum.ON_SHELF, updatedProduct.getSaleStatus());
        assertNotNull(updatedProduct.getAuditTime());
        assertNotNull(updatedProduct.getPublishTime());
    }

    @Test
    void offShelfProductShouldRequireSellerAndSetOffShelfStatus() {
        Product product = approvedOnShelfProduct();
        when(productMapper.selectById(10L)).thenReturn(product);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        BaseException exception = assertThrows(BaseException.class, () -> productService.offShelfProduct(3L, 10L));
        assertEquals("只能操作自己的商品", exception.getMessage());

        productService.offShelfProduct(2L, 10L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).updateById(captor.capture());
        Product updatedProduct = captor.getValue();

        assertEquals(ProductSaleStatusEnum.OFF_SHELF, updatedProduct.getSaleStatus());
        assertNotNull(updatedProduct.getOffShelfTime());
    }

    @Test
    void removeProductByAdminShouldRequireReasonAndSetRemovedStatus() {
        Product product = approvedOnShelfProduct();
        AdminProductRemoveDTO dto = new AdminProductRemoveDTO();
        dto.setReason("违规商品");

        when(productMapper.selectById(10L)).thenReturn(product);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        productService.removeProductByAdmin(10L, dto.getReason());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).updateById(captor.capture());
        Product updatedProduct = captor.getValue();

        assertEquals(ProductSaleStatusEnum.REMOVED_BY_ADMIN, updatedProduct.getSaleStatus());
        assertNotNull(updatedProduct.getOffShelfTime());
    }

    private ProductCategory enabledCategory() {
        ProductCategory category = new ProductCategory();
        category.setId(1L);
        category.setName("教材图书");
        category.setSortOrder(1);
        category.setStatus(CategoryStatusEnum.ENABLED);
        return category;
    }

    private Product approvedOnShelfProduct() {
        Product product = new Product();
        product.setId(10L);
        product.setSellerId(2L);
        product.setCategoryId(1L);
        product.setTitle("高等数学教材");
        product.setDescription("九成新");
        product.setPrice(new BigDecimal("25.00"));
        product.setConditionLevel(com.cshm.campussecondhandmark.module.product.enums.ProductConditionEnum.LIKE_NEW);
        product.setCoverImageUrl("https://example.com/1.jpg");
        product.setAuditStatus(ProductAuditStatusEnum.APPROVED);
        product.setSaleStatus(ProductSaleStatusEnum.ON_SHELF);
        product.setPublishTime(LocalDateTime.now());
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        return product;
    }

    private ProductImage productImage() {
        ProductImage image = new ProductImage();
        image.setProductId(10L);
        image.setImageUrl("https://example.com/1.jpg");
        image.setSortOrder(1);
        return image;
    }

    private User seller() {
        User user = new User();
        user.setId(2L);
        user.setNickname("张三");
        user.setAvatarUrl("https://example.com/avatar.jpg");
        user.setCampusVerifyStatus(CampusVerifyStatusEnum.APPROVED);
        return user;
    }
}
