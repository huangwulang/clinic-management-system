package com.guet.clinic.server.service;

import com.guet.clinic.pojo.entity.Patient;
import java.time.LocalDate;

public interface PatientService extends CrudService<Patient> {
    Patient setMemberLevel(Long id, String levelCode, String memberName, LocalDate expireDate);
}
