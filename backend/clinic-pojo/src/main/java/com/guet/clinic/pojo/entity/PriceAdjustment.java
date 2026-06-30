package com.guet.clinic.pojo.entity;

import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PriceAdjustment extends BaseEntity {
    private Long drugId;
    private String drugCode;
    private String drugName;
    private BigDecimal oldPurchasePrice;
    private BigDecimal newPurchasePrice;
    private BigDecimal oldSellPrice;
    private BigDecimal newSellPrice;
    private String operator;
    private String reason;
}
