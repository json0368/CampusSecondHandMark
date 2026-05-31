package com.cshm.campussecondhandmark.module.user.controller;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.result.PageResult;
import com.cshm.campussecondhandmark.module.user.enums.CampusVerifyStatusEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import com.cshm.campussecondhandmark.module.user.pojo.vo.AdminUserDetailVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.AdminUserListVO;
import com.cshm.campussecondhandmark.module.user.pojo.vo.CampusVerifyAuditVO;
import com.cshm.campussecondhandmark.module.user.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminUserController controller = new AdminUserController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(noopValidator())
                .build();
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void campusVerifyAdminEndpointsShouldUseAdminApiPath() throws Exception {
        CampusVerifyAuditVO auditVO = new CampusVerifyAuditVO();
        auditVO.setId(3L);
        auditVO.setUsername("zhangsan");

        when(userService.pageCampusVerifyUsers(any())).thenReturn(new PageResult<>(1, List.of(auditVO)));

        mockMvc.perform(get("/admin-api/users/campus-verify/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(3));

        BaseContext.setCurrentId(7L);
        mockMvc.perform(put("/admin-api/users/3/campus-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campusVerifyStatus\":\"REJECTED\",\"remark\":\"学号与专业信息不一致\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(userService).pageCampusVerifyUsers(any());
        verify(userService).auditCampusVerify(eq(3L), eq(7L), any());
    }

    @Test
    void adminUserManageEndpointsShouldUseAdminApiPath() throws Exception {
        AdminUserListVO listVO = new AdminUserListVO();
        listVO.setId(3L);
        listVO.setUsername("zhangsan");
        listVO.setStatus(UserStatusEnum.BANNED);
        listVO.setBanReason("发布违规商品");

        AdminUserDetailVO detailVO = new AdminUserDetailVO();
        detailVO.setId(3L);
        detailVO.setUsername("zhangsan");
        detailVO.setNickname("张三");
        detailVO.setRole(UserRoleEnum.USER);
        detailVO.setStatus(UserStatusEnum.BANNED);
        detailVO.setCampusVerifyStatus(CampusVerifyStatusEnum.APPROVED);
        detailVO.setBanReason("发布违规商品");
        detailVO.setUnbanReason("已恢复");

        when(userService.pageAdminUsers(any())).thenReturn(new PageResult<>(1, List.of(listVO)));
        when(userService.getAdminUserDetail(3L)).thenReturn(detailVO);

        mockMvc.perform(get("/admin-api/users")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "BANNED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(3))
                .andExpect(jsonPath("$.data.records[0].banReason").value("发布违规商品"));

        mockMvc.perform(get("/admin-api/users/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.username").value("zhangsan"))
                .andExpect(jsonPath("$.data.role").value("USER"));

        BaseContext.setCurrentId(7L);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/admin-api/users/3/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"发布违规商品\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        BaseContext.setCurrentId(7L);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/admin-api/users/3/unban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"已完成整改\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(userService).pageAdminUsers(any());
        verify(userService).getAdminUserDetail(3L);
        verify(userService).banUser(eq(3L), eq(7L), any());
        verify(userService).unbanUser(eq(3L), eq(7L), any());
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
