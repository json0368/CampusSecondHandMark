package com.cshm.campussecondhandmark.module.product.service.support;

import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.properties.ChatServiceProperties;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ChatConversationOpenRequest;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductConversationOpenVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.time.Duration;

@Component
@Slf4j
public class ChatServiceClient {

    private static final String OPEN_PRODUCT_CONVERSATION_PATH = "/internal/product-conversations/open";
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 5000;

    private final ChatServiceProperties chatServiceProperties;
    private final RestOperations restOperations;

    public ChatServiceClient(ChatServiceProperties chatServiceProperties, RestTemplateBuilder restTemplateBuilder) {
        this(chatServiceProperties, restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(normalizeTimeout(chatServiceProperties.getConnectTimeoutMs(), DEFAULT_CONNECT_TIMEOUT_MS)))
                .setReadTimeout(Duration.ofMillis(normalizeTimeout(chatServiceProperties.getReadTimeoutMs(), DEFAULT_READ_TIMEOUT_MS)))
                .build());
    }

    ChatServiceClient(ChatServiceProperties chatServiceProperties, RestOperations restOperations) {
        this.chatServiceProperties = chatServiceProperties;
        this.restOperations = restOperations;
    }

    public ProductConversationOpenVO openProductConversation(ChatConversationOpenRequest request) {
        if (!chatServiceProperties.isEnabled() || !StringUtils.hasText(chatServiceProperties.getBaseUrl())) {
            throw new BaseException("通信服务未配置");
        }
        try {
            ResponseEntity<ProductConversationOpenVO> response = restOperations.exchange(
                    buildOpenUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(request, buildHeaders()),
                    ProductConversationOpenVO.class);
            ProductConversationOpenVO body = response.getBody();
            if (body == null || !StringUtils.hasText(body.getConversationId())
                    || !StringUtils.hasText(body.getMatrixRoomId())
                    || !StringUtils.hasText(body.getChatTicket())) {
                throw new BaseException("通信服务返回异常");
            }
            return body;
        } catch (BaseException exception) {
            throw exception;
        } catch (RestClientException exception) {
            log.error("调用通信服务开通商品会话失败", exception);
            throw new BaseException("创建商品会话失败，请稍后重试");
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(chatServiceProperties.getInternalToken())) {
            headers.setBearerAuth(chatServiceProperties.getInternalToken());
        }
        return headers;
    }

    private String buildOpenUrl() {
        String baseUrl = chatServiceProperties.getBaseUrl().trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + OPEN_PRODUCT_CONVERSATION_PATH;
    }

    private static int normalizeTimeout(Integer timeoutMs, int defaultTimeoutMs) {
        return timeoutMs == null || timeoutMs < 1 ? defaultTimeoutMs : timeoutMs;
    }
}
