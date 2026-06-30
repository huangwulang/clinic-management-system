package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.ConsultationSectionDTO;
import com.guet.clinic.pojo.entity.Consultation;
import com.guet.clinic.server.service.ConsultationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prescriptions")
public class PrescriptionController {
    private final ConsultationService service;
    public PrescriptionController(ConsultationService service) { this.service = service; }
    @PutMapping("/{visitId}") public Result<Consultation> update(@PathVariable Long visitId, @RequestBody ConsultationSectionDTO dto) {
        return Result.success(service.savePrescription(visitId, dto));
    }
    @DeleteMapping("/{visitId}") public Result<Consultation> delete(@PathVariable Long visitId) {
        return Result.success(service.clearPrescription(visitId));
    }
}
