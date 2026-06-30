package com.guet.clinic.server.controller;

import com.guet.clinic.common.exception.BusinessException;
import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.PasswordUpdateDTO;
import com.guet.clinic.pojo.entity.UserAccount;
import com.guet.clinic.server.service.AuthTokenService;
import com.guet.clinic.server.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final UserAccountService userAccountService;
    private final AuthTokenService authTokenService;
    public AccountController(UserAccountService userAccountService, AuthTokenService authTokenService) {
        this.userAccountService = userAccountService;
        this.authTokenService = authTokenService;
    }
    @GetMapping("/profile") public Result<UserAccount> profile(HttpServletRequest request) {
        return Result.success(userAccountService.get(current(request).getId()));
    }
    @PutMapping("/profile") public Result<UserAccount> profile(HttpServletRequest request, @RequestBody UserAccount dto) {
        dto.setPassword(null);
        return Result.success(userAccountService.update(current(request).getId(), dto));
    }
    @PutMapping("/password") public Result<UserAccount> password(HttpServletRequest request, @Valid @RequestBody PasswordUpdateDTO dto) {
        return Result.success(userAccountService.changePassword(current(request).getId(), dto.getOldPassword(), dto.getNewPassword()));
    }
    private UserAccount current(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : request.getHeader("token");
        UserAccount user = authTokenService.getUser(token);
        if (user == null) throw new BusinessException("登录状态已失效");
        return user;
    }
}
