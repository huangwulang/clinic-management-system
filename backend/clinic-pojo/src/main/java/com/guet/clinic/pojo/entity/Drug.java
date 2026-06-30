package com.guet.clinic.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Drug extends BaseEntity {
    private String drugCode;
    private String barcode;
    @NotBlank
    private String name;
    private String pinyin;
    private String specification;
    private String category;
    private String dosageForm;
    private Boolean otc;
    private String invoiceItem;
    private String unit;
    private String packUnit;
    private BigDecimal baseRatio;
    private String baseUnit;
    private BigDecimal doseRatio;
    private String doseUnit;
    private BigDecimal purchasePrice;
    private BigDecimal sellPrice;
    private String manufacturer;
    private String approvalNo;
    private String status;
    private Integer warningStock;
    private Integer stockMin;
    private Integer stockMax;
    private String locationNo;
    private Integer expiryWarningDays;
    private Boolean allowSplit;
    private Boolean allowMemberDiscount;
    private String usageMethod;
    private BigDecimal singleDose;
    private String frequency;
    private Integer defaultDays;
    private BigDecimal defaultTotal;
    private String totalUnit;
    private String description;
    private String remark;
}
