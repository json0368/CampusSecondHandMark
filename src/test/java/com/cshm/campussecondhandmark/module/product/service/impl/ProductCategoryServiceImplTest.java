package com.cshm.campussecondhandmark.module.product.service.impl;

import com.cshm.campussecondhandmark.module.product.enums.CategoryStatusEnum;
import com.cshm.campussecondhandmark.module.product.mapper.ProductCategoryMapper;
import com.cshm.campussecondhandmark.module.product.pojo.entity.ProductCategory;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductCategoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceImplTest {

    @Mock
    private ProductCategoryMapper productCategoryMapper;

    private ProductCategoryServiceImpl productCategoryService;

    @BeforeEach
    void setUp() {
        productCategoryService = new ProductCategoryServiceImpl();
        ReflectionTestUtils.setField(productCategoryService, "baseMapper", productCategoryMapper);
    }

    @Test
    void listEnabledCategoriesShouldMapCategoryVOs() {
        ProductCategory category = new ProductCategory();
        category.setId(1L);
        category.setName("教材图书");
        category.setParentId(null);
        category.setSortOrder(1);
        category.setStatus(CategoryStatusEnum.ENABLED);

        when(productCategoryMapper.selectList(any())).thenReturn(List.of(category));

        List<ProductCategoryVO> categories = productCategoryService.listEnabledCategories();

        assertEquals(1, categories.size());
        assertEquals(1L, categories.get(0).getId());
        assertEquals("教材图书", categories.get(0).getName());
        assertEquals(1, categories.get(0).getSortOrder());
    }
}
