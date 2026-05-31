package com.cshm.campussecondhandmark.common.interceptor;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.properties.JwtProperties;
import com.cshm.campussecondhandmark.common.utils.JwtUtil;
import com.cshm.campussecondhandmark.module.user.enums.UserRoleEnum;
import com.cshm.campussecondhandmark.module.user.enums.UserStatusEnum;
import com.cshm.campussecondhandmark.module.user.pojo.entity.User;
import com.cshm.campussecondhandmark.module.user.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        boolean publicProductRequest = isPublicProductRequest(request);
        String token = request.getHeader(jwtProperties.getTokenName());
        if (publicProductRequest && !StringUtils.hasText(token)) {
            return true;
        }

        try {
            log.info("JWT token: {}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get("id").toString());
            String tokenType = claims.get("tokenType").toString();
            if (!"user".equals(tokenType)) {
                response.setStatus(401);
                return false;
            }

            User user = userService.get(userId);
            if (user.getRole() != UserRoleEnum.USER || user.getStatus() == UserStatusEnum.BANNED) {
                throw new IllegalStateException("user token is not allowed");
            }

            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception ex) {
            if (publicProductRequest) {
                return true;
            }
            response.setStatus(401);
            return false;
        }
    }

    private boolean isPublicProductRequest(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return "/api/products".equals(path) || path.matches("^/api/products/\\d+$");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
