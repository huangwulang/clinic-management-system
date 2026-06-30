package com.guet.clinic.server.service;

import com.guet.clinic.pojo.dto.ConsultationSectionDTO;
import com.guet.clinic.pojo.entity.ChargeOrder;
import com.guet.clinic.pojo.entity.Consultation;

import java.util.List;

public interface ConsultationService extends CrudService<Consultation> {
    List<Consultation> listByPatientId(Long patientId);
    Consultation updatePatient(Long id, ConsultationSectionDTO dto);
    Consultation saveMedicalRecord(Long id, ConsultationSectionDTO dto);
    Consultation savePrescription(Long id, ConsultationSectionDTO dto);
    Consultation clearPrescription(Long id);
    Consultation finish(Long id);
    ChargeOrder createChargeOrder(Long id);
}
