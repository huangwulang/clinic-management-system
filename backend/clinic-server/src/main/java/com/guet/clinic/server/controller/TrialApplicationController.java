package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.TrialApplicationDTO;
import com.guet.clinic.pojo.entity.TrialApplication;
import com.guet.clinic.server.service.TrialApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/trial-applications", "/trial/applications"})
public class TrialApplicationController {
    private final TrialApplicationService service;
    public TrialApplicationController(TrialApplicationService service) { this.service = service; }

    @PostMapping({"", "/apply"})
    public Result<TrialApplication> apply(@Valid @RequestBody TrialApplicationDTO dto) {
        TrialApplication entity = new TrialApplication();
        entity.setClinicName(dto.getClinicName());
        entity.setContactName(dto.getContactName());
        entity.setPhone(dto.getPhone());
        entity.setRegion(dto.getRegion());
        entity.setScale(dto.getScale());
        entity.setRemark(dto.getRemark());
        entity.setStatus("PENDING");
        return Result.success(service.save(entity));
    }
}
