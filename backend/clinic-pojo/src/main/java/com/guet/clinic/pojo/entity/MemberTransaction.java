package com.guet.clinic.pojo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MemberTransaction extends BaseEntity {
    private Long memberId;
    private String transactionType;
    private BigDecimal amount;
    private Integer points;
    private BigDecimal balanceAfter;
    private Integer pointsAfter;
    private String businessNo;
    private String remark;
    private String operator;
}
