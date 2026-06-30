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
public class ChargeOrder extends BaseEntity {
    @NotBlank
    private String orderNo;
    private Long registrationId;
    private Long patientId;
    private String patientName;
    private String gender;
    private Integer age;
    private String phone;
    private String chargeType;
    private String departmentName;
    private String doctorName;
    private BigDecimal receivableAmount;
    private BigDecimal discountAmount;
    private BigDecimal paidAmount;
    private BigDecimal refundAmount;
    private String payMethod;
    private String refundMethod;
    private String status;
    private String cashier;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private String remark;
}
