package com.guet.clinic.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MemberAdjustDTO {
    @NotNull private BigDecimal amount;
    private String transactionType;
    private String businessNo;
    private String remark;
    private String operator;
}
