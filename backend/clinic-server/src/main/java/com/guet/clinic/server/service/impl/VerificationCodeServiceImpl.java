package com.guet.clinic.server.service.impl;

import com.guet.clinic.server.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {
    private static final String CODE_KEY_PREFIX = "clinic:verification-code:";

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${clinic.redis.verification-code-ttl-minutes:5}")
    private long verificationCodeTtlMinutes;

    public VerificationCodeServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String send(String phone) {
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        stringRedisTemplate.opsForValue().set(cacheKey(phone), code, Duration.ofMinutes(verificationCodeTtlMinutes));
        return code;
    }

    @Override
    public boolean verify(String phone, String code) {
        String cachedCode = stringRedisTemplate.opsForValue().get(cacheKey(phone));
        if (cachedCode == null || !cachedCode.equals(code)) return false;
        stringRedisTemplate.delete(cacheKey(phone));
        return true;
    }

    private String cacheKey(String phone) {
        return CODE_KEY_PREFIX + phone;
    }
}
