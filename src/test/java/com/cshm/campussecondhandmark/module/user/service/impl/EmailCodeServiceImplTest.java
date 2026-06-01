package com.cshm.campussecondhandmark.module.user.service.impl;

import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.properties.AuthCodeProperties;
import com.cshm.campussecondhandmark.common.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailCodeServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private MailService mailService;

    private EmailCodeServiceImpl emailCodeService;

    @BeforeEach
    void setUp() {
        emailCodeService = new EmailCodeServiceImpl();
        ReflectionTestUtils.setField(emailCodeService, "stringRedisTemplate", stringRedisTemplate);
        ReflectionTestUtils.setField(emailCodeService, "mailService", mailService);

        AuthCodeProperties authCodeProperties = new AuthCodeProperties();
        authCodeProperties.setExpireSeconds(300);
        authCodeProperties.setCooldownSeconds(60);
        authCodeProperties.setLength(6);
        authCodeProperties.setSubjectForgotPassword("找回密码验证码");
        ReflectionTestUtils.setField(emailCodeService, "authCodeProperties", authCodeProperties);

        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void sendForgotPasswordCodeShouldStoreCodeInRedisAndSendMail() {
        when(stringRedisTemplate.hasKey("cshm:auth:code:cooldown:forgot-password:zhangsan@example.com")).thenReturn(false);

        emailCodeService.sendForgotPasswordCode("zhangsan@example.com");

        verify(valueOperations).set(eq("cshm:auth:code:forgot-password:zhangsan@example.com"), anyString(), eq(300L), eq(TimeUnit.SECONDS));
        verify(valueOperations).set("cshm:auth:code:cooldown:forgot-password:zhangsan@example.com", "1", 60L, TimeUnit.SECONDS);
        verify(mailService).sendForgotPasswordCode(eq("zhangsan@example.com"), anyString(), eq(300L));
    }

    @Test
    void sendForgotPasswordCodeShouldRejectWhenCooldownExists() {
        when(stringRedisTemplate.hasKey("cshm:auth:code:cooldown:forgot-password:zhangsan@example.com")).thenReturn(true);

        assertThrows(BaseException.class, () -> emailCodeService.sendForgotPasswordCode("zhangsan@example.com"));

        verify(mailService, never()).sendForgotPasswordCode(eq("zhangsan@example.com"), anyString(), anyLong());
    }

    @Test
    void sendRegisterCodeShouldStoreCodeInRedisAndSendMail() {
        when(stringRedisTemplate.hasKey("cshm:auth:code:cooldown:register:zhangsan@example.com")).thenReturn(false);

        emailCodeService.sendRegisterCode("zhangsan@example.com");

        verify(valueOperations).set(eq("cshm:auth:code:register:zhangsan@example.com"), anyString(), eq(300L), eq(TimeUnit.SECONDS));
        verify(valueOperations).set("cshm:auth:code:cooldown:register:zhangsan@example.com", "1", 60L, TimeUnit.SECONDS);
        verify(mailService).sendRegisterCode(eq("zhangsan@example.com"), anyString(), eq(300L));
    }

    @Test
    void sendRegisterCodeShouldRejectWhenCooldownExists() {
        when(stringRedisTemplate.hasKey("cshm:auth:code:cooldown:register:zhangsan@example.com")).thenReturn(true);

        assertThrows(BaseException.class, () -> emailCodeService.sendRegisterCode("zhangsan@example.com"));

        verify(mailService, never()).sendRegisterCode(eq("zhangsan@example.com"), anyString(), anyLong());
    }

    @Test
    void verifyForgotPasswordCodeShouldPassAndDeleteCache() {
        when(valueOperations.get("cshm:auth:code:forgot-password:zhangsan@example.com")).thenReturn("123456");

        emailCodeService.verifyForgotPasswordCode("zhangsan@example.com", "123456");

        verify(stringRedisTemplate).delete("cshm:auth:code:forgot-password:zhangsan@example.com");
    }

    @Test
    void verifyRegisterCodeShouldPassAndDeleteCache() {
        when(valueOperations.get("cshm:auth:code:register:zhangsan@example.com")).thenReturn("123456");

        emailCodeService.verifyRegisterCode("zhangsan@example.com", "123456");

        verify(stringRedisTemplate).delete("cshm:auth:code:register:zhangsan@example.com");
    }

    @Test
    void verifyForgotPasswordCodeShouldRejectWhenCodeMismatch() {
        when(valueOperations.get("cshm:auth:code:forgot-password:zhangsan@example.com")).thenReturn("654321");

        assertThrows(BaseException.class, () -> emailCodeService.verifyForgotPasswordCode("zhangsan@example.com", "123456"));

        verify(stringRedisTemplate, never()).delete("cshm:auth:code:forgot-password:zhangsan@example.com");
    }

    @Test
    void verifyRegisterCodeShouldRejectWhenCodeMismatch() {
        when(valueOperations.get("cshm:auth:code:register:zhangsan@example.com")).thenReturn("654321");

        assertThrows(BaseException.class, () -> emailCodeService.verifyRegisterCode("zhangsan@example.com", "123456"));

        verify(stringRedisTemplate, never()).delete("cshm:auth:code:register:zhangsan@example.com");
    }
}
