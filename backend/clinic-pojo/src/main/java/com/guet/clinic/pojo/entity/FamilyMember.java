package com.guet.clinic.pojo.entity;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class FamilyMember extends BaseEntity {
    private Long patientId;
    private String relation;
    @NotBlank(message = "家庭成员姓名不能为空")
    private String name;
    private String gender;
    private Integer age;
    private String company;
    private LocalDate birthday;
    private String phone;
}
