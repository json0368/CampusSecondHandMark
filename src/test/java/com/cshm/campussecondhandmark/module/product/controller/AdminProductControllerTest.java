package com.cshm.campussecondhandmark.module.product.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductAuditVO;
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
class AdminProductControllerTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminProductController adminProductController = new AdminProductController();
        ReflectionTestUtils.setField(adminProductController, "productService", productService);
        mockMvc = MockMvcBuilders.standaloneSetup(adminProductController)
                .setValidator(noopValidator())
                .build();
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void pageAuditProductsShouldUseAdminApiPath() throws Exception {
        ProductAuditVO productAuditVO = new ProductAuditVO();
        productAuditVO.setId(10L);
        productAuditVO.setTitle("高等数学教材");
        when(productService.pageAuditProducts(any())).thenReturn(new PageResult<>(1, List.of(productAuditVO)));

        mockMvc.perform(get("/admin-api/products/audit")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(10));

        verify(productService).pageAuditProducts(any());
    }

    @Test
    void auditAndRemoveProductShouldUseAdminApiPath() throws Exception {
        BaseContext.setCurrentId(1L);

        mockMvc.perform(post("/admin-api/products/10/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"auditStatus\":\"APPROVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(post("/admin-api/products/10/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"违规商品\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(productService).auditProduct(eq(10L), any());
        verify(productService).removeProductByAdmin(eq(10L), eq("违规商品"));
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
