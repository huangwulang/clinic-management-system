package com.guet.clinic.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TrialApplicationDTO {
    @NotBlank private String clinicName;
    @NotBlank private String contactName;
    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;
    @NotBlank private String region;
    @NotBlank private String scale;
    private String remark;
}
