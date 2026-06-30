package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.server.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workbench")
public class WorkbenchController {
    private final ReportService service;
    public WorkbenchController(ReportService service) { this.service = service; }
    @GetMapping("/visits")
    public Result<List<Map<String, Object>>> visits(
            @RequestParam(required = false) String status, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department, @RequestParam(required = false) String doctor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(service.workbenchVisits(status, keyword, department, doctor, startDate, endDate));
    }
}
