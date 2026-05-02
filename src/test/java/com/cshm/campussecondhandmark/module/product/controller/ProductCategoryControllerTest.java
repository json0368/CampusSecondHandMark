package com.cshm.campussecondhandmark.module.product.controller;

import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductCategoryVO;
import com.cshm.campussecondhandmark.module.product.service.ProductCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductCategoryControllerTest {

    @Mock
    private ProductCategoryService productCategoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProductCategoryController productCategoryController = new ProductCategoryController();
        ReflectionTestUtils.setField(productCategoryController, "productCategoryService", productCategoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(productCategoryController)
                .setValidator(noopValidator())
                .build();
    }

    @Test
    void listCategoriesShouldUseApiCategoriesPath() throws Exception {
        ProductCategoryVO categoryVO = new ProductCategoryVO();
        categoryVO.setId(1L);
        categoryVO.setName("教材图书");
        categoryVO.setSortOrder(1);
        when(productCategoryService.listEnabledCategories()).thenReturn(List.of(categoryVO));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].name").value("教材图书"));

        verify(productCategoryService).listEnabledCategories();
    }

    @Test
    void legacyProductCategoriesPathShouldNotBeSupported() throws Exception {
        mockMvc.perform(get("/api/product-categories"))
                .andExpect(status().isNotFound());
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
