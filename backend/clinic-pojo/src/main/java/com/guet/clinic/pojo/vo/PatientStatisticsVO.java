package com.guet.clinic.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientStatisticsVO {
    private long patientCount;
    private long memberCount;
    private long registrationCount;
    private long consultationCount;
}
