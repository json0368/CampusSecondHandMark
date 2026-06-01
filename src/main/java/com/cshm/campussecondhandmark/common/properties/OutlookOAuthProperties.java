package com.cshm.campussecondhandmark.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cshm.mail.outlook")
@Data
public class OutlookOAuthProperties {

    private String host;

    private Integer port;

    private String clientId;

    private String clientSecret;

    private String redirectUri;

    private String authorizeUrl;

    private String tokenUrl;

    private String scope;

    private String refreshToken;

    private String mailFrom;

    private Integer stateExpireSeconds;
}
