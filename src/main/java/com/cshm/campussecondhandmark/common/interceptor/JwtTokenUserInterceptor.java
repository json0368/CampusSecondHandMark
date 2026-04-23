package com.cshm.campussecondhandmark.common.interceptor;

import com.cshm.campussecondhandmark.common.context.BaseContext;
import com.cshm.campussecondhandmark.common.properties.JwtProperties;
import com.cshm.campussecondhandmark.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getTokenName());
        try {
            log.info("JWT token: {}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get("id").toString());
            String tokenType = claims.get("tokenType").toString();
            if (!"user".equals(tokenType)) {
                response.setStatus(401);
                return false;
            }

            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception ex) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
