package com.guet.clinic.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Registration extends BaseEntity {
    private String registrationNo;
    private Long patientId;
    @NotBlank
    private String patientName;
    private String phone;
    private String gender;
    private Integer age;
    private String departmentName;
    private Long doctorId;
    private String doctorName;
    private String visitType;
    private LocalDateTime visitTime;
    private String status;
    private BigDecimal registrationFee;
    private BigDecimal diagnosisFee;
    private String operator;
    private String remark;
    private String chargeOrderNo;
    private BigDecimal receivableAmount;
    private BigDecimal discountAmount;
    private BigDecimal paidAmount;
    private String payMethod;
    private LocalDateTime paidAt;
    private String cashier;
}
