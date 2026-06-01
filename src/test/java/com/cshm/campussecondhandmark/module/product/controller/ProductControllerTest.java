package com.cshm.campussecondhandmark.module.product.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.product.pojo.vo.MyProductVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductDetailVO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductSummaryVO;
import com.cshm.campussecondhandmark.module.product.service.ProductInteractionService;
import com.cshm.campussecondhandmark.module.product.service.ProductService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductInteractionService productInteractionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProductController productController = new ProductController();
        ReflectionTestUtils.setField(productController, "productService", productService);
        ReflectionTestUtils.setField(productController, "productInteractionService", productInteractionService);
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setValidator(noopValidator())
                .build();
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void createProductShouldUseApiProductsPath() throws Exception {
        BaseContext.setCurrentId(2L);
        when(productService.createProduct(eq(2L), any())).thenReturn(100L);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"title\":\"高等数学教材\",\"price\":25.00,\"conditionLevel\":1,\"imageUrls\":[\"https://example.com/1.jpg\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value(100));

        verify(productService).createProduct(eq(2L), any());
    }

    @Test
    void pageProductsShouldUsePublicApiProductsPath() throws Exception {
        ProductSummaryVO summaryVO = new ProductSummaryVO();
        summaryVO.setId(10L);
        summaryVO.setTitle("高等数学教材");
        summaryVO.setPrice(new BigDecimal("25.00"));

        when(productService.pageProducts(any())).thenReturn(new PageResult<>(1, List.of(summaryVO)));

        mockMvc.perform(get("/api/products")
                        .param("keyword", "教材")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("高等数学教材"));

        verify(productService).pageProducts(any());
    }

    @Test
    void getProductDetailShouldUseApiProductsIdPath() throws Exception {
        BaseContext.setCurrentId(3L);
        ProductDetailVO detailVO = new ProductDetailVO();
        detailVO.setId(10L);
        detailVO.setTitle("高等数学教材");
        detailVO.setCanOrder(true);

        when(productService.getProductDetail(10L, 3L)).thenReturn(detailVO);

        mockMvc.perform(get("/api/products/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.canOrder").value(true));

        verify(productService).getProductDetail(10L, 3L);
    }

    @Test
    void pageMyProductsShouldUseApiProductsMePath() throws Exception {
        BaseContext.setCurrentId(2L);
        MyProductVO myProductVO = new MyProductVO();
        myProductVO.setId(10L);
        myProductVO.setTitle("高等数学教材");

        when(productService.pageMyProducts(eq(2L), any())).thenReturn(new PageResult<>(1, List.of(myProductVO)));

        mockMvc.perform(get("/api/products/me")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(10));

        verify(productService).pageMyProducts(eq(2L), any());
    }

    @Test
    void updateAndOffShelfProductShouldUseApiProductPaths() throws Exception {
        BaseContext.setCurrentId(2L);

        mockMvc.perform(put("/api/products/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"高等数学教材新版\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(post("/api/products/10/off-shelf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(productService).updateProduct(eq(2L), eq(10L), any());
        verify(productService).offShelfProduct(eq(2L), eq(10L));
    }

    @Test
    void productInteractionEndpointsShouldUseApiProductsPaths() throws Exception {
        BaseContext.setCurrentId(3L);
        ProductSummaryVO summaryVO = new ProductSummaryVO();
        summaryVO.setId(10L);
        summaryVO.setTitle("高等数学教材");

        when(productInteractionService.pageMyFavorites(eq(3L), any())).thenReturn(new PageResult<>(1, List.of(summaryVO)));
        when(productInteractionService.pageMyBrowseHistory(eq(3L), any())).thenReturn(new PageResult<>(1, List.of(summaryVO)));

        mockMvc.perform(post("/api/products/10/favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(delete("/api/products/10/favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/api/products/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").value(10));

        mockMvc.perform(get("/api/products/browse-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").value(10));

        mockMvc.perform(delete("/api/products/browse-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(productInteractionService).favoriteProduct(3L, 10L);
        verify(productInteractionService).unfavoriteProduct(3L, 10L);
        verify(productInteractionService).pageMyFavorites(eq(3L), any());
        verify(productInteractionService).pageMyBrowseHistory(eq(3L), any());
        verify(productInteractionService).clearMyBrowseHistory(3L);
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
