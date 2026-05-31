package com.cshm.campussecondhandmark.common.interceptor;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.properties.JwtProperties;
import com.cshm.campussecondhandmark.common.utils.JwtUtil;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import com.cshm.campussecondhandmark.module.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenUserInterceptorTest {

    @Mock
    private UserService userService;

    private JwtTokenUserInterceptor interceptor;

    private HandlerMethod handlerMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        interceptor = new JwtTokenUserInterceptor();

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setTokenName("token");
        jwtProperties.setUserSecretKey("user-secret");
        ReflectionTestUtils.setField(interceptor, "jwtProperties", jwtProperties);
        ReflectionTestUtils.setField(interceptor, "userService", userService);

        Method method = DummyController.class.getDeclaredMethod("handle");
        handlerMethod = new HandlerMethod(new DummyController(), method);
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void preHandleShouldAllowNormalUserToken() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.NORMAL);

        when(userService.get(1L)).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/me");
        request.addHeader("token", buildUserToken(1L));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(allowed);
        assertEquals(1L, BaseContext.getCurrentId());
    }

    @Test
    void preHandleShouldRejectBannedUserToken() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRoleEnum.USER);
        user.setStatus(UserStatusEnum.BANNED);

        when(userService.get(1L)).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/me");
        request.addHeader("token", buildUserToken(1L));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, handlerMethod);

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
        assertNull(BaseContext.getCurrentId());
    }

    @Test
    void preHandleShouldAllowAnonymousPublicProductRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(allowed);
        verifyNoInteractions(userService);
    }

    private String buildUserToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("role", UserRoleEnum.USER.getCode());
        claims.put("tokenType", "user");
        return JwtUtil.createJWT("user-secret", 7200000L, claims);
    }

    private static class DummyController {
        public void handle() {
        }
    }
}
