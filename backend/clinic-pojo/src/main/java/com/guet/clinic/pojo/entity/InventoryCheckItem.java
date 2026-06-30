package com.guet.clinic.pojo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InventoryCheckItem extends BaseEntity {
    private Long checkId;
    private Long inventoryId;
    private Long drugId;
    private String drugCode;
    private String drugName;
    private String locationNo;
    private String prescriptionCategory;
    private String specification;
    private String manufacturer;
    private Integer systemQuantity;
    private Integer checkQuantity;
    private Integer differenceQuantity;
    private String unit;
    private String remark;
}
