package com.guet.clinic.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class FeeItem extends BaseEntity {
    private String feeCode;
    @NotBlank
    private String name;
    private String category;
    private BigDecimal price;
    private String unit;
    private String status;
    private String remark;
}
