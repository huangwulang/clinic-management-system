package com.guet.clinic.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberPointsAdjustDTO {
    @NotNull private Integer points;
    private String transactionType;
    private String businessNo;
    private String remark;
    private String operator;
}
