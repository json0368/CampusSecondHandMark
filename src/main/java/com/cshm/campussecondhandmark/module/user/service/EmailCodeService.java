package com.cshm.campussecondhandmark.module.user.service;

public interface EmailCodeService {

    void sendRegisterCode(String email);

    void verifyRegisterCode(String email, String code);

    void sendForgotPasswordCode(String email);

    void verifyForgotPasswordCode(String email, String code);
}
