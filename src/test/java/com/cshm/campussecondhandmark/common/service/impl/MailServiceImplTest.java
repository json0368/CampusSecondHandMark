package com.cshm.campussecondhandmark.common.service.impl;

import com.cshm.campussecondhandmark.common.properties.AuthCodeProperties;
import com.cshm.campussecondhandmark.common.properties.OutlookOAuthProperties;
import com.cshm.campussecondhandmark.common.service.OutlookOAuthTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private OutlookOAuthTokenService outlookOAuthTokenService;

    private MailServiceImpl mailService;

    @BeforeEach
    void setUp() {
        mailService = spy(new MailServiceImpl());

        AuthCodeProperties authCodeProperties = new AuthCodeProperties();
        authCodeProperties.setSubjectForgotPassword("找回密码验证码");
        ReflectionTestUtils.setField(mailService, "authCodeProperties", authCodeProperties);
        ReflectionTestUtils.setField(mailService, "outlookOAuthTokenService", outlookOAuthTokenService);

        OutlookOAuthProperties outlookOAuthProperties = new OutlookOAuthProperties();
        outlookOAuthProperties.setHost("smtp-mail.outlook.com");
        outlookOAuthProperties.setPort(587);
        outlookOAuthProperties.setMailFrom("sender@outlook.com");
        ReflectionTestUtils.setField(mailService, "outlookOAuthProperties", outlookOAuthProperties);
    }

    @Test
    void sendForgotPasswordCodeShouldRequestAccessTokenBeforeSend() {
        when(outlookOAuthTokenService.getAccessToken()).thenReturn("access-token");
        doReturn(javaMailSender).when(mailService).createMailSender("access-token");

        mailService.sendForgotPasswordCode("zhangsan@example.com", "123456", 300L);

        verify(outlookOAuthTokenService).getAccessToken();

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("sender@outlook.com", message.getFrom());
        assertEquals("zhangsan@example.com", message.getTo()[0]);
        assertTrue(message.getText().contains("123456"));
        assertTrue(message.getText().contains("5"));
    }

    @Test
    void sendRegisterCodeShouldRequestAccessTokenBeforeSend() {
        when(outlookOAuthTokenService.getAccessToken()).thenReturn("access-token");
        doReturn(javaMailSender).when(mailService).createMailSender("access-token");

        mailService.sendRegisterCode("zhangsan@example.com", "123456", 300L);

        verify(outlookOAuthTokenService).getAccessToken();

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("sender@outlook.com", message.getFrom());
        assertEquals("zhangsan@example.com", message.getTo()[0]);
        assertTrue(message.getText().contains("123456"));
        assertTrue(message.getText().contains("5"));
    }

    @Test
    void createMailSenderShouldUseStarttlsAndXoauth2() {
        JavaMailSender sender = mailService.createMailSender("access-token");
        JavaMailSenderImpl mailSender = (JavaMailSenderImpl) sender;

        assertEquals("smtp-mail.outlook.com", mailSender.getHost());
        assertEquals(587, mailSender.getPort());
        assertEquals("sender@outlook.com", mailSender.getUsername());
        assertEquals("access-token", mailSender.getPassword());
        assertEquals("true", mailSender.getJavaMailProperties().getProperty("mail.smtp.auth"));
        assertEquals("true", mailSender.getJavaMailProperties().getProperty("mail.smtp.starttls.enable"));
        assertEquals("true", mailSender.getJavaMailProperties().getProperty("mail.smtp.starttls.required"));
        assertEquals("XOAUTH2", mailSender.getJavaMailProperties().getProperty("mail.smtp.auth.mechanisms"));
    }
}
