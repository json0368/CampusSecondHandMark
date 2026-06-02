package com.cshm.campussecondhandmark.module.report.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.report.pojo.vo.AdminReportDetailVO;
import com.cshm.campussecondhandmark.module.report.pojo.vo.AdminReportListVO;
import com.cshm.campussecondhandmark.module.report.service.ReportService;
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
class AdminReportControllerTest {

    @Mock
    private ReportService reportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminReportController controller = new AdminReportController();
        ReflectionTestUtils.setField(controller, "reportService", reportService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(noopValidator())
                .build();
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void adminReportEndpointsShouldDelegateService() throws Exception {
        BaseContext.setCurrentId(7L);
        AdminReportListVO listVO = new AdminReportListVO();
        listVO.setId(100L);
        when(reportService.pageAdminReports(any())).thenReturn(new PageResult<>(1, List.of(listVO)));
        when(reportService.getAdminReportDetail(100L)).thenReturn(new AdminReportDetailVO());

        mockMvc.perform(get("/admin-api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(100));

        mockMvc.perform(get("/admin-api/reports/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(post("/admin-api/reports/100/handle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handleResult\":\"VALID\",\"handleRemark\":\"已核实\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(reportService).pageAdminReports(any());
        verify(reportService).getAdminReportDetail(100L);
        verify(reportService).handleReport(eq(100L), eq(7L), any());
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
