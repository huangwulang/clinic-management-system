package com.guet.clinic.server.controller;

import com.guet.clinic.common.exception.BusinessException;
import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.LoginDTO;
import com.guet.clinic.pojo.dto.PasswordResetDTO;
import com.guet.clinic.pojo.dto.SmsCodeDTO;
import com.guet.clinic.pojo.entity.UserAccount;
import com.guet.clinic.pojo.vo.LoginVO;
import com.guet.clinic.server.service.AuthTokenService;
import com.guet.clinic.server.service.UserAccountService;
import com.guet.clinic.server.service.VerificationCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserAccountService userAccountService;
    private final AuthTokenService authTokenService;
    private final VerificationCodeService verificationCodeService;

    public AuthController(UserAccountService userAccountService, AuthTokenService authTokenService,
                          VerificationCodeService verificationCodeService) {
        this.userAccountService = userAccountService;
        this.authTokenService = authTokenService;
        this.verificationCodeService = verificationCodeService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) { return Result.success(userAccountService.login(dto)); }
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody LoginDTO dto) { return Result.success(userAccountService.register(dto)); }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        authTokenService.invalidate(resolveToken(request));
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserAccount> me(HttpServletRequest request) {
        UserAccount user = authTokenService.getUser(resolveToken(request));
        if (user == null) throw new BusinessException("登录状态已失效");
        return Result.success(userAccountService.get(user.getId()));
    }

    @PostMapping({"/sms-code", "/password/code"})
    public Result<Map<String, Object>> sendCode(@Valid @RequestBody SmsCodeDTO dto) {
        String code = verificationCodeService.send(dto.getPhone());
        return Result.success(Map.of("expiresIn", 300, "developmentCode", code));
    }

    @RequestMapping(value = "/password/reset", method = {RequestMethod.POST, RequestMethod.PUT})
    public Result<UserAccount> reset(@Valid @RequestBody PasswordResetDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) throw new BusinessException("两次输入的密码不一致");
        if (!verificationCodeService.verify(dto.getPhone(), dto.getCode())) throw new BusinessException("验证码错误或已过期");
        return Result.success(userAccountService.resetPassword(dto.getPhone(), dto.getPassword()));
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) return authorization.substring(7);
        return request.getHeader("token");
    }
}
