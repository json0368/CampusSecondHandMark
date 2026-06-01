package com.cshm.campussecondhandmark.common.service.impl;

import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.properties.OutlookOAuthProperties;
import com.cshm.campussecondhandmark.common.service.OutlookOAuthTokenService;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.OutlookOAuthAuthorizeVO;
import com.cshm.campussecondhandmark.module.admin.pojo.vo.OutlookOAuthCallbackVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OutlookOAuthTokenServiceImpl implements OutlookOAuthTokenService {

    private static final String OAUTH_STATE_KEY_PREFIX = "cshm:mail:outlook:oauth:state:";

    @Autowired
    private OutlookOAuthProperties outlookOAuthProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private RestOperations restOperations = new RestTemplate();

    @Override
    public OutlookOAuthAuthorizeVO buildAuthorizeRequest() {
        validateAuthorizeConfig();

        String state = generateState();
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set(buildStateKey(state), "1", safeStateExpireSeconds(), TimeUnit.SECONDS);

        OutlookOAuthAuthorizeVO vo = new OutlookOAuthAuthorizeVO();
        vo.setAuthorizeUrl(buildAuthorizeUrl(state));
        vo.setExpireSeconds(safeStateExpireSeconds());
        return vo;
    }

    @Override
    public OutlookOAuthCallbackVO handleAuthorizationCallback(String code, String state, String error, String errorDescription) {
        validateAuthorizeConfig();

        if (StringUtils.hasText(error)) {
            throw new BaseException("Outlook 授权失败");
        }
        if (!StringUtils.hasText(code)) {
            throw new BaseException("授权码不能为空");
        }
        if (!StringUtils.hasText(state)) {
            throw new BaseException("授权状态不能为空");
        }
        if (!Boolean.TRUE.equals(stringRedisTemplate.delete(buildStateKey(state)))) {
            throw new BaseException("授权状态不存在或已过期");
        }

        MultiValueMap<String, String> formData = baseTokenFormData();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", outlookOAuthProperties.getRedirectUri());

        Map<String, Object> response = requestToken(formData);
        String refreshToken = readRequiredTokenValue(response, "refresh_token", "未获取到 refresh token");

        OutlookOAuthCallbackVO vo = new OutlookOAuthCallbackVO();
        vo.setRefreshToken(refreshToken);
        vo.setMessage("请将 refresh token 写回配置文件");
        return vo;
    }

    @Override
    public String getAccessToken() {
        validateRefreshTokenConfig();

        MultiValueMap<String, String> formData = baseTokenFormData();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", outlookOAuthProperties.getRefreshToken());

        Map<String, Object> response = requestToken(formData);
        return readRequiredTokenValue(response, "access_token", "未获取到 access token");
    }

    private String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromHttpUrl(outlookOAuthProperties.getAuthorizeUrl())
                .queryParam("client_id", outlookOAuthProperties.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", outlookOAuthProperties.getRedirectUri())
                .queryParam("response_mode", "query")
                .queryParam("scope", outlookOAuthProperties.getScope())
                .queryParam("state", state)
                .encode(StandardCharsets.UTF_8)
                .toUriString();
    }

    private Map<String, Object> requestToken(MultiValueMap<String, String> formData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, Object> response = restOperations.postForObject(
                outlookOAuthProperties.getTokenUrl(),
                new HttpEntity<>(formData, headers),
                Map.class);
        if (response == null || response.isEmpty()) {
            throw new BaseException("Outlook token 请求失败");
        }
        return response;
    }

    private MultiValueMap<String, String> baseTokenFormData() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", outlookOAuthProperties.getClientId());
        formData.add("client_secret", outlookOAuthProperties.getClientSecret());
        formData.add("scope", outlookOAuthProperties.getScope());
        return formData;
    }

    private String readRequiredTokenValue(Map<String, Object> response, String key, String message) {
        Object value = response.get(key);
        if (!StringUtils.hasText(value == null ? null : value.toString())) {
            throw new BaseException(message);
        }
        return value.toString();
    }

    private void validateAuthorizeConfig() {
        if (!StringUtils.hasText(outlookOAuthProperties.getClientId())
                || !StringUtils.hasText(outlookOAuthProperties.getClientSecret())
                || !StringUtils.hasText(outlookOAuthProperties.getRedirectUri())
                || !StringUtils.hasText(outlookOAuthProperties.getAuthorizeUrl())
                || !StringUtils.hasText(outlookOAuthProperties.getTokenUrl())
                || !StringUtils.hasText(outlookOAuthProperties.getScope())) {
            throw new BaseException("Outlook OAuth 配置不完整");
        }
    }

    private void validateRefreshTokenConfig() {
        validateAuthorizeConfig();
        if (!StringUtils.hasText(outlookOAuthProperties.getRefreshToken())) {
            throw new BaseException("未配置 Outlook refresh token");
        }
    }

    private String generateState() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String buildStateKey(String state) {
        return OAUTH_STATE_KEY_PREFIX + state;
    }

    private int safeStateExpireSeconds() {
        return outlookOAuthProperties.getStateExpireSeconds() == null || outlookOAuthProperties.getStateExpireSeconds() < 1
                ? 300 : outlookOAuthProperties.getStateExpireSeconds();
    }
}
