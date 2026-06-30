package com.guet.clinic.pojo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Member extends BaseEntity {
    private Long patientId;
    private String cardNo;
    private String patientName;
    private String phone;
    private String memberType;
    private String levelName;
    private BigDecimal totalConsume;
    private BigDecimal balance;
    private BigDecimal totalStored;
    private Integer points;
    private LocalDate openDate;
    private LocalDate expireDate;
    private String status;
}
