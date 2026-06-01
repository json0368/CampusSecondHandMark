package com.cshm.campussecondhandmark.common.service.impl;

import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.properties.OutlookOAuthProperties;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.OutlookOAuthAuthorizeVO;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.OutlookOAuthCallbackVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutlookOAuthTokenServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RestOperations restOperations;

    private OutlookOAuthTokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new OutlookOAuthTokenServiceImpl();
        ReflectionTestUtils.setField(tokenService, "stringRedisTemplate", stringRedisTemplate);
        ReflectionTestUtils.setField(tokenService, "restOperations", restOperations);

        OutlookOAuthProperties properties = new OutlookOAuthProperties();
        properties.setClientId("client-id");
        properties.setClientSecret("client-secret");
        properties.setRedirectUri("http://localhost:8086/admin-api/mail/outlook/oauth/callback");
        properties.setAuthorizeUrl("https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize");
        properties.setTokenUrl("https://login.microsoftonline.com/consumers/oauth2/v2.0/token");
        properties.setScope("offline_access https://outlook.office.com/SMTP.Send");
        properties.setRefreshToken("refresh-token");
        properties.setStateExpireSeconds(300);
        ReflectionTestUtils.setField(tokenService, "outlookOAuthProperties", properties);

        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void buildAuthorizeRequestShouldPersistStateAndReturnAuthorizeUrl() {
        OutlookOAuthAuthorizeVO vo = tokenService.buildAuthorizeRequest();

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), eq("1"), eq(300L), eq(TimeUnit.SECONDS));

        String key = keyCaptor.getValue();
        String state = key.substring("cshm:mail:outlook:oauth:state:".length());

        assertEquals(300, vo.getExpireSeconds());
        assertTrue(vo.getAuthorizeUrl().contains("client_id=client-id"));
        assertTrue(vo.getAuthorizeUrl().contains("response_type=code"));
        assertTrue(vo.getAuthorizeUrl().contains("state=" + state));
    }

    @Test
    void handleAuthorizationCallbackShouldRejectWhenStateMissing() {
        when(stringRedisTemplate.delete("cshm:mail:outlook:oauth:state:state-1")).thenReturn(false);

        assertThrows(BaseException.class,
                () -> tokenService.handleAuthorizationCallback("auth-code", "state-1", null, null));
    }

    @Test
    void handleAuthorizationCallbackShouldExchangeCodeForRefreshToken() {
        when(stringRedisTemplate.delete("cshm:mail:outlook:oauth:state:state-1")).thenReturn(true);

        Map<String, Object> response = new HashMap<>();
        response.put("access_token", "access-token");
        response.put("refresh_token", "new-refresh-token");
        when(restOperations.postForObject(eq("https://login.microsoftonline.com/consumers/oauth2/v2.0/token"),
                any(HttpEntity.class), eq(Map.class))).thenReturn(response);

        OutlookOAuthCallbackVO vo = tokenService.handleAuthorizationCallback("auth-code", "state-1", null, null);

        assertEquals("new-refresh-token", vo.getRefreshToken());

        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restOperations).postForObject(eq("https://login.microsoftonline.com/consumers/oauth2/v2.0/token"),
                captor.capture(), eq(Map.class));

        MultiValueMap<String, String> body = captor.getValue().getBody();
        assertEquals("authorization_code", body.getFirst("grant_type"));
        assertEquals("auth-code", body.getFirst("code"));
        assertEquals("client-id", body.getFirst("client_id"));
    }

    @Test
    void getAccessTokenShouldUseRefreshTokenGrant() {
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", "access-token");
        when(restOperations.postForObject(eq("https://login.microsoftonline.com/consumers/oauth2/v2.0/token"),
                any(HttpEntity.class), eq(Map.class))).thenReturn(response);

        String accessToken = tokenService.getAccessToken();

        assertEquals("access-token", accessToken);

        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restOperations).postForObject(eq("https://login.microsoftonline.com/consumers/oauth2/v2.0/token"),
                captor.capture(), eq(Map.class));

        MultiValueMap<String, String> body = captor.getValue().getBody();
        assertEquals("refresh_token", body.getFirst("grant_type"));
        assertEquals("refresh-token", body.getFirst("refresh_token"));
        assertEquals("offline_access https://outlook.office.com/SMTP.Send", body.getFirst("scope"));
    }
}
