package com.guet.clinic.pojo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Inventory extends BaseEntity {
    private Long drugId;
    private String drugCode;
    private String drugName;
    private String locationNo;
    private String batchNo;
    private String category;
    private String prescriptionCategory;
    private String specification;
    private String dosageForm;
    private String manufacturer;
    private Integer quantity;
    private String unit;
    private BigDecimal purchaseAmount;
    private BigDecimal retailAmount;
    private LocalDate expireDate;
    private Integer warningStock;
}
