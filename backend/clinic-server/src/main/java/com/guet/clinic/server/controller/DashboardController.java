package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.server.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final ReportService service;
    public DashboardController(ReportService service) { this.service = service; }
    @GetMapping("/summary") public Result<Map<String, Object>> summary() { return Result.success(service.dashboardSummary()); }
    @GetMapping("/trends") public Result<List<Map<String, Object>>> trends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(service.dashboardTrends(startDate, endDate));
    }
    @GetMapping("/rankings") public Result<List<Map<String, Object>>> rankings(
            @RequestParam(defaultValue = "doctor") String type, @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(service.dashboardRankings(type, limit));
    }
}
