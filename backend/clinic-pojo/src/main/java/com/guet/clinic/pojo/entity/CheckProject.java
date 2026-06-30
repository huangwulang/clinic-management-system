package com.guet.clinic.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CheckProject extends BaseEntity {
    private String projectCode;
    @NotBlank
    private String name;
    private String category;
    private BigDecimal costPrice;
    private BigDecimal retailPrice;
    private BigDecimal price;
    private String unit;
    private String invoiceItem;
    private String bodyPart;
    private Boolean allowMemberDiscount;
    private String status;
    private String creator;
    private String remark;
}
