package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.ConsultationSectionDTO;
import com.guet.clinic.pojo.entity.ChargeOrder;
import com.guet.clinic.pojo.entity.Consultation;
import com.guet.clinic.server.service.ConsultationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/consultations", "/visits"})
public class ConsultationController extends CrudController<Consultation> {
    private final ConsultationService consultationService;
    public ConsultationController(ConsultationService consultationService) { this.consultationService = consultationService; }
    @Override protected ConsultationService service() { return consultationService; }
    @PutMapping("/{id}/patient") public Result<Consultation> patient(@PathVariable Long id, @RequestBody ConsultationSectionDTO dto) {
        return Result.success(consultationService.updatePatient(id, dto));
    }
    @PutMapping("/{id}/medical-record") public Result<Consultation> record(@PathVariable Long id, @RequestBody ConsultationSectionDTO dto) {
        return Result.success(consultationService.saveMedicalRecord(id, dto));
    }
    @PostMapping("/{id}/prescriptions") public Result<Consultation> prescription(@PathVariable Long id, @RequestBody ConsultationSectionDTO dto) {
        return Result.success(consultationService.savePrescription(id, dto));
    }
    @PostMapping("/{id}/finish") public Result<Consultation> finish(@PathVariable Long id) {
        return Result.success(consultationService.finish(id));
    }
    @PostMapping("/{id}/charge-order") public Result<ChargeOrder> charge(@PathVariable Long id) {
        return Result.success(consultationService.createChargeOrder(id));
    }
}
