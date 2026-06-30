package com.guet.clinic.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {
    private String roleCode;
    @NotBlank
    private String name;
    private String description;
    private String permissions;
    private String creator;
    private Boolean enabled;
}
