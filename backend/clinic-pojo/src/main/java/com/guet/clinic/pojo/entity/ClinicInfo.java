package com.guet.clinic.pojo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ClinicInfo extends BaseEntity {
    private String clinicCode;
    private String clinicName;
    private String principal;
    private String phone;
    private String email;
    private String region;
    private String address;
    private String licenseNo;
    private String introduction;
}
