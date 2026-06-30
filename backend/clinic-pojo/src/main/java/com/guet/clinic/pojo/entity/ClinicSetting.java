package com.guet.clinic.pojo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ClinicSetting extends BaseEntity {
    private String settingKey;
    private String settingValue;
    private String description;
}
