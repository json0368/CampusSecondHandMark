package com.cshm.campussecondhandmark.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cshm.chat")
@Data
public class ChatServiceProperties {

    private boolean enabled;

    private String baseUrl;

    private String internalToken;

    private Integer connectTimeoutMs;

    private Integer readTimeoutMs;
}
