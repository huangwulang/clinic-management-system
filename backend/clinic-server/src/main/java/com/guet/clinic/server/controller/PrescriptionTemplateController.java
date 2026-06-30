package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.entity.MedicalTemplate;
import com.guet.clinic.server.service.MedicalTemplateService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/prescription-templates")
public class PrescriptionTemplateController {
    private final MedicalTemplateService service;
    public PrescriptionTemplateController(MedicalTemplateService service) { this.service = service; }
    @GetMapping public Result<List<MedicalTemplate>> list(@RequestParam(required = false) String keyword) {
        return Result.success(service.list(keyword).stream().filter(v -> "PRESCRIPTION".equalsIgnoreCase(v.getTemplateType())).toList());
    }
    @GetMapping("/{id}") public Result<MedicalTemplate> get(@PathVariable Long id) { return Result.success(service.get(id)); }
    @PostMapping public Result<MedicalTemplate> create(@RequestBody MedicalTemplate value) {
        value.setTemplateType("PRESCRIPTION");
        return Result.success(service.save(value));
    }
    @PutMapping("/{id}") public Result<MedicalTemplate> update(@PathVariable Long id, @RequestBody MedicalTemplate value) {
        value.setTemplateType("PRESCRIPTION");
        return Result.success(service.update(id, value));
    }
    @DeleteMapping("/{id}") public Result<Void> delete(@PathVariable Long id) { service.delete(id); return Result.ok(); }
}
