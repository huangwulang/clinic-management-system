package com.guet.clinic.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetDTO {
    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;
    @NotBlank
    private String code;
    @NotBlank
    @Size(min = 8, max = 20)
    private String password;
    @NotBlank
    private String confirmPassword;
}
