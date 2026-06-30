package com.guet.clinic.server.service;

public interface VerificationCodeService {
    String send(String phone);
    boolean verify(String phone, String code);
}
