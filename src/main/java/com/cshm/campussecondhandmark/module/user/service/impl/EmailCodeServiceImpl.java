package com.cshm.campussecondhandmark.module.user.service.impl;

import com.cshm.campussecondhandmark.common.exception.BaseException;
import com.cshm.campussecondhandmark.common.properties.AuthCodeProperties;
import com.cshm.campussecondhandmark.common.service.MailService;
import com.cshm.campussecondhandmark.module.user.service.EmailCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class EmailCodeServiceImpl implements EmailCodeService {

    private static final String REGISTER_CODE_KEY_PREFIX = "cshm:auth:code:register:";
    private static final String REGISTER_COOLDOWN_KEY_PREFIX = "cshm:auth:code:cooldown:register:";
    private static final String FORGOT_PASSWORD_CODE_KEY_PREFIX = "cshm:auth:code:forgot-password:";
    private static final String FORGOT_PASSWORD_COOLDOWN_KEY_PREFIX = "cshm:auth:code:cooldown:forgot-password:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MailService mailService;

    @Autowired
    private AuthCodeProperties authCodeProperties;

    @Override
    public void sendRegisterCode(String email) {
        sendCode(email, REGISTER_CODE_KEY_PREFIX, REGISTER_COOLDOWN_KEY_PREFIX, true);
    }

    @Override
    public void verifyRegisterCode(String email, String code) {
        verifyCode(email, code, REGISTER_CODE_KEY_PREFIX);
    }

    @Override
    public void sendForgotPasswordCode(String email) {
        sendCode(email, FORGOT_PASSWORD_CODE_KEY_PREFIX, FORGOT_PASSWORD_COOLDOWN_KEY_PREFIX, false);
    }

    @Override
    public void verifyForgotPasswordCode(String email, String code) {
        verifyCode(email, code, FORGOT_PASSWORD_CODE_KEY_PREFIX);
    }

    private void sendCode(String email, String codeKeyPrefix, String cooldownKeyPrefix, boolean registerScene) {
        String cooldownKey = cooldownKeyPrefix + email;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            throw new BaseException("验证码发送过于频繁");
        }

        String code = generateCode(authCodeProperties.getLength());
        stringRedisTemplate.opsForValue().set(codeKeyPrefix + email, code, authCodeProperties.getExpireSeconds(), TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(cooldownKey, "1", authCodeProperties.getCooldownSeconds(), TimeUnit.SECONDS);
        if (registerScene) {
            mailService.sendRegisterCode(email, code, authCodeProperties.getExpireSeconds());
        } else {
            mailService.sendForgotPasswordCode(email, code, authCodeProperties.getExpireSeconds());
        }
    }

    private void verifyCode(String email, String code, String codeKeyPrefix) {
        String key = codeKeyPrefix + email;
        String cachedCode = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(cachedCode) || !cachedCode.equals(code)) {
            throw new BaseException("验证码错误或已过期");
        }
        stringRedisTemplate.delete(key);
    }

    private String generateCode(int length) {
        int safeLength = Math.max(1, length);
        int bound = (int) Math.pow(10, safeLength);
        int value = ThreadLocalRandom.current().nextInt(bound);
        return String.format("%0" + safeLength + "d", value);
    }
}
