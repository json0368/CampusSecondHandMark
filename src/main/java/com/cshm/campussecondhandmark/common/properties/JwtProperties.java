package com.cshm.campussecondhandmark.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cshm.jwt")
@Data
public class JwtProperties {

    private String tokenName;

    private String adminSecretKey;
    private long adminTtl;

    private String userSecretKey;
    private long userTtl;
}
