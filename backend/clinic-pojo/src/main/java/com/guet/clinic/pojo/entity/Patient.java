package com.guet.clinic.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Patient extends BaseEntity {
    private String patientCode;
    @NotBlank
    private String name;
    private String cardNo;
    private Integer age;
    private LocalDate birthday;
    private String gender;
    private String phone;
    private String idCard;
    private String source;
    private String memberLevel;
    private LocalDate memberExpireDate;
    private String nation;
    private String marriage;
    private String education;
    private String provinceCity;
    private String address;
    private String job;
    private String company;
    private String remark;
    private String operator;
}
