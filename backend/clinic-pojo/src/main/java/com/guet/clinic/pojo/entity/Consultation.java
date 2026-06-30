package com.guet.clinic.pojo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Consultation extends BaseEntity {
    private Long registrationId;
    private Long patientId;
    private String patientName;
    private String doctorName;
    private String departmentName;
    private String visitType;
    private String chiefComplaint;
    private String diagnosis;
    private String doctorAdvice;
    private String vitalSigns;
    private String medicalRecord;
    private String prescription;
    private String checkItems;
    private String status;
}
