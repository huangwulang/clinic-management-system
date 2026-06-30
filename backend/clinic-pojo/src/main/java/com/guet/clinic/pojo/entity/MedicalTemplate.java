package com.guet.clinic.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MedicalTemplate extends BaseEntity {
    private String templateCode;
    private String templateType;
    @NotBlank
    private String name;
    private String permission;
    private String diagnosis;
    private String description;
    private String content;
    private String creator;
    private String status;
}
