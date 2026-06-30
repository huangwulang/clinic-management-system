package com.guet.clinic.server.service;

import com.guet.clinic.pojo.entity.UserAccount;

public interface AuthTokenService {
    String createToken(UserAccount userAccount);

    boolean validateToken(String token);

    UserAccount getUser(String token);

    void invalidate(String token);
}
