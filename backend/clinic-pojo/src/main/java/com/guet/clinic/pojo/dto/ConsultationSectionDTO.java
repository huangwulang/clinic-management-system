package com.guet.clinic.pojo.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ConsultationSectionDTO {
    private Long patientId;
    private String patientName;
    private String diagnosis;
    private String doctorAdvice;
    private Map<String, Object> vitalSign;
    private Map<String, Object> medicalRecord;
    private Map<String, Object> prescription;
}
