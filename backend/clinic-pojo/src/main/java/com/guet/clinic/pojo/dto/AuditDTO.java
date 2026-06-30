package com.guet.clinic.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditDTO {
    @NotNull private Boolean approved;
    private String remark;
}
