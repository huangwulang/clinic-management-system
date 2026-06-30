package com.guet.clinic.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Department extends BaseEntity {
    private String departmentCode;
    @NotBlank
    private String name;
    private String description;
    private String creator;
    private Boolean enabled;
}
