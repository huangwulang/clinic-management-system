package com.guet.clinic.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Supplier extends BaseEntity {
    @NotBlank
    private String supplierCode;
    @NotBlank
    private String name;
    private String contactName;
    private String phone;
    private String status;
    private String creator;
    private String remark;
}
