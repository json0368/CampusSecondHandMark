package com.cshm.campussecondhandmark.common.service.impl;

import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.properties.AuthCodeProperties;
import com.cshm.campussecondhandmark.common.properties.OutlookOAuthProperties;
import com.cshm.campussecondhandmark.common.service.MailService;
import com.cshm.campussecondhandmark.common.service.OutlookOAuthTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Service
public class MailServiceImpl implements MailService {

    private static final String SUBJECT_REGISTER = "注册邮箱验证码";

    @Autowired
    private AuthCodeProperties authCodeProperties;

    @Autowired
    private OutlookOAuthProperties outlookOAuthProperties;

    @Autowired
    private OutlookOAuthTokenService outlookOAuthTokenService;

    @Override
    public void sendRegisterCode(String email, String code, long expireSeconds) {
        sendCodeMail(email, SUBJECT_REGISTER, buildRegisterContent(code, expireSeconds));
    }

    @Override
    public void sendForgotPasswordCode(String email, String code, long expireSeconds) {
        sendCodeMail(email, authCodeProperties.getSubjectForgotPassword(), buildForgotPasswordContent(code, expireSeconds));
    }

    JavaMailSender createMailSender(String accessToken) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(outlookOAuthProperties.getHost());
        mailSender.setPort(outlookOAuthProperties.getPort() == null ? 587 : outlookOAuthProperties.getPort());
        mailSender.setUsername(outlookOAuthProperties.getMailFrom());
        mailSender.setPassword(accessToken);
        mailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());

        Properties properties = mailSender.getJavaMailProperties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.starttls.required", "true");
        properties.setProperty("mail.smtp.auth.mechanisms", "XOAUTH2");
        return mailSender;
    }

    private void sendCodeMail(String email, String subject, String content) {
        validateMailConfig();

        String accessToken = outlookOAuthTokenService.getAccessToken();
        JavaMailSender mailSender = createMailSender(accessToken);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(outlookOAuthProperties.getMailFrom());
        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            throw new BaseException("邮件发送失败");
        }
    }

    private void validateMailConfig() {
        if (!StringUtils.hasText(outlookOAuthProperties.getHost())
                || !StringUtils.hasText(outlookOAuthProperties.getMailFrom())) {
            throw new BaseException("Outlook 发信配置不完整");
        }
    }

    private String buildRegisterContent(String code, long expireSeconds) {
        long expireMinutes = Math.max(1, expireSeconds / 60);
        return "您好，您的注册验证码为：" + code + "，有效期 " + expireMinutes + " 分钟。若非本人操作，请忽略本邮件。";
    }

    private String buildForgotPasswordContent(String code, long expireSeconds) {
        long expireMinutes = Math.max(1, expireSeconds / 60);
        return "您好，您的找回密码验证码为：" + code + "，有效期 " + expireMinutes + " 分钟。若非本人操作，请忽略本邮件。";
    }
}
