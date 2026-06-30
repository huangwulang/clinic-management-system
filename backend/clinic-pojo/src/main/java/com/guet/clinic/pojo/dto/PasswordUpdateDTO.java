package com.guet.clinic.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordUpdateDTO {
    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;
}
