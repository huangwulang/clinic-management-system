package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.server.service.ClinicSettingService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/system/settings")
public class ClinicSettingController {
    private final ClinicSettingService service;
    public ClinicSettingController(ClinicSettingService service) { this.service = service; }
    @GetMapping public Result<Map<String, String>> get() { return Result.success(service.getSettings()); }
    @PutMapping public Result<Map<String, String>> update(@RequestBody Map<String, String> settings) { return Result.success(service.saveSettings(settings)); }
}
