package com.guet.clinic.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Staff extends BaseEntity {
    private String jobNo;
    @NotBlank
    private String name;
    private String gender;
    private Integer age;
    private String phone;
    private String email;
    private String idCard;
    private String positionName;
    private String clinicName;
    private String departmentName;
    private String roleName;
    private String address;
    private String creator;
    private Boolean enabled;
}
