package com.guet.clinic.pojo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InventoryLog extends BaseEntity {
    private Long inventoryId;
    private Long drugId;
    private String businessType;
    private String businessNo;
    private Integer changeQuantity;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private String operator;
    private String remark;
}
