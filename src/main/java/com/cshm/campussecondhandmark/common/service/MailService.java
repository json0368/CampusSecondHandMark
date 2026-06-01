package com.cshm.campussecondhandmark.common.service;

public interface MailService {

    void sendRegisterCode(String email, String code, long expireSeconds);

    void sendForgotPasswordCode(String email, String code, long expireSeconds);
}
