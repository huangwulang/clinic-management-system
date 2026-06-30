package com.guet.clinic.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class UserAccount extends BaseEntity {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String name;
    private String phone;
    private String roleName;
    private String permissions;
    private Boolean enabled;
}
