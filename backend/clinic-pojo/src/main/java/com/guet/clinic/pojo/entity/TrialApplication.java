package com.guet.clinic.pojo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class TrialApplication extends BaseEntity {
    private String clinicName;
    private String contactName;
    private String phone;
    private String region;
    private String scale;
    private String remark;
    private String status;
}
