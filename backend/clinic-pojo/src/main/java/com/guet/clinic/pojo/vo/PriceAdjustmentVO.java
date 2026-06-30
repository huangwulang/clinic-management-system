package com.guet.clinic.pojo.vo;

import com.guet.clinic.pojo.entity.PriceAdjustment;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PriceAdjustmentVO extends PriceAdjustment {
    private Long inventoryId;
    private String prescriptionCategory;
    private String specification;
    private String dosageForm;
    private String manufacturer;
    private String batchNo;
    private Integer quantity;
    private String unit;
    private BigDecimal purchaseAmount;
    private BigDecimal retailAmount;
    private Integer adjustmentCount;
}
