package com.cshm.campussecondhandmark.module.product.service.support;

import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.properties.ChatServiceProperties;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ChatConversationOpenRequest;
import com.cshm.campussecondhandmark.module.product.pojo.dto.ChatUserProfileDTO;
import com.cshm.campussecondhandmark.module.product.pojo.vo.ProductConversationOpenVO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ChatServiceClientTest {

    @Test
    void openProductConversationShouldPostToConfiguredRustEndpoint() {
        ChatServiceProperties properties = new ChatServiceProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("http://chat-service:9001");
        properties.setInternalToken("internal-token");

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
        server.expect(once(), requestTo("http://chat-service:9001/internal/product-conversations/open"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer internal-token"))
                .andExpect(content().string(containsString("\"buyerId\":2")))
                .andRespond(withSuccess("{\"conversationId\":\"pc_001\",\"matrixRoomId\":\"!room:im.example.com\",\"chatTicket\":\"ticket_001\",\"ticketExpireSeconds\":60}",
                        MediaType.APPLICATION_JSON));

        ChatServiceClient client = new ChatServiceClient(properties, restTemplate);

        ProductConversationOpenVO result = client.openProductConversation(buildRequest());

        assertEquals("pc_001", result.getConversationId());
        assertEquals("ticket_001", result.getChatTicket());
        server.verify();
    }

    @Test
    void openProductConversationShouldRejectMissingBaseUrl() {
        ChatServiceProperties properties = new ChatServiceProperties();
        properties.setEnabled(true);

        ChatServiceClient client = new ChatServiceClient(properties, new RestTemplate());

        BaseException exception = assertThrows(BaseException.class,
                () -> client.openProductConversation(buildRequest()));

        assertEquals("通信服务未配置", exception.getMessage());
    }

    private ChatConversationOpenRequest buildRequest() {
        ChatConversationOpenRequest request = new ChatConversationOpenRequest();
        request.setBusinessKey("buyer:2:seller:3:product:10");
        request.setBuyerId(2L);
        request.setSellerId(3L);
        request.setProductId(10L);
        request.setProductTitle("九成新机械键盘");
        request.setProductCoverUrl("https://example.com/product/10-cover.jpg");
        request.setBuyerProfile(profile("买家同学", "https://example.com/avatar/2.png"));
        request.setSellerProfile(profile("卖家同学", "https://example.com/avatar/3.png"));
        return request;
    }

    private ChatUserProfileDTO profile(String nickname, String avatarUrl) {
        ChatUserProfileDTO profile = new ChatUserProfileDTO();
        profile.setNickname(nickname);
        profile.setAvatarUrl(avatarUrl);
        return profile;
    }
}
