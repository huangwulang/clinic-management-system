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
public class StockOrder extends BaseEntity {
    @NotBlank
    private String orderNo;
    private String stockDirection;
    private String stockType;
    private Long supplierId;
    private String supplierName;
    private String maker;
    private BigDecimal purchaseAmount;
    private BigDecimal retailAmount;
    private String operator;
    private String auditStatus;
    private LocalDateTime auditAt;
    private String remark;
}
