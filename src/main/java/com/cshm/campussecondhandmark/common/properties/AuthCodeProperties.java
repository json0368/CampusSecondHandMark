package com.cshm.campussecondhandmark.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cshm.auth.code")
@Data
public class AuthCodeProperties {

    private int expireSeconds;

    private int cooldownSeconds;

    private int length;

    private String subjectForgotPassword;
}
