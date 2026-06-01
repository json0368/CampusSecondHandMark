package com.cshm.campussecondhandmark.module.admin.controller;

import com.cshm.campussecondhandmark.common.service.OutlookOAuthTokenService;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.OutlookOAuthAuthorizeVO;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.OutlookOAuthCallbackVO;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OutlookOAuthAdminControllerTest {

    @Mock
    private OutlookOAuthTokenService outlookOAuthTokenService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        OutlookOAuthAdminController controller = new OutlookOAuthAdminController();
        ReflectionTestUtils.setField(controller, "outlookOAuthTokenService", outlookOAuthTokenService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(new Validator() {
                    @Override
                    public boolean supports(Class<?> clazz) {
                        return true;
                    }

                    @Override
                    public void validate(Object target, Errors errors) {
                    }
                })
                .build();
    }

    @Test
    void authorizeShouldSupportAdminApiMailOutlookOauthAuthorizePath() throws Exception {
        OutlookOAuthAuthorizeVO vo = new OutlookOAuthAuthorizeVO();
        vo.setAuthorizeUrl("https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?state=abc");
        vo.setExpireSeconds(300);

        when(outlookOAuthTokenService.buildAuthorizeRequest()).thenReturn(vo);

        mockMvc.perform(get("/admin-api/mail/outlook/oauth/authorize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.authorizeUrl").value("https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?state=abc"))
                .andExpect(jsonPath("$.data.expireSeconds").value(300));

        verify(outlookOAuthTokenService).buildAuthorizeRequest();
    }

    @Test
    void callbackShouldSupportAdminApiMailOutlookOauthCallbackPath() throws Exception {
        OutlookOAuthCallbackVO vo = new OutlookOAuthCallbackVO();
        vo.setRefreshToken("refresh-token");
        vo.setMessage("请将 refresh token 写回配置文件");

        when(outlookOAuthTokenService.handleAuthorizationCallback("auth-code", "state-1", null, null)).thenReturn(vo);

        mockMvc.perform(get("/admin-api/mail/outlook/oauth/callback")
                        .param("code", "auth-code")
                        .param("state", "state-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.message").value("请将 refresh token 写回配置文件"));

        verify(outlookOAuthTokenService).handleAuthorizationCallback("auth-code", "state-1", null, null);
    }
}
