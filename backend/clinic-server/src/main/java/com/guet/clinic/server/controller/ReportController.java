package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.server.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/reports", "/statistics"})
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/charge/{type}")
    public Result<List<Map<String, Object>>> charge(
            @PathVariable String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String cashier,
            @RequestParam(required = false) String doctor,
            @RequestParam(required = false) String tradeType) {
        return Result.success(service.chargeStatistics(type, startDate, endDate, orderType, keyword, department, cashier, doctor, tradeType));
    }

    @GetMapping("/charge/detail/{id}")
    public Result<Map<String, Object>> chargeDetail(@PathVariable Long id) {
        return Result.success(service.chargeOrderDetail(id));
    }

    @GetMapping("/patients/{type}")
    public Result<List<Map<String, Object>>> patients(
            @PathVariable String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String doctor,
            @RequestParam(required = false) String chargeStatus) {
        return Result.success(service.patientStatistics(type, startDate, endDate, keyword, doctor, chargeStatus));
    }

    @GetMapping("/drugs/{type}")
    public Result<List<Map<String, Object>>> drugs(
            @PathVariable String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String stockInType,
            @RequestParam(required = false) String stockOutType,
            @RequestParam(required = false) String orderType) {
        return Result.success(service.drugStatistics(type, startDate, endDate, keyword, stockInType, stockOutType, orderType));
    }

    @GetMapping("/check-projects")
    public Result<List<Map<String, Object>>> projects(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword) {
        return Result.success(service.checkProjectStatistics(startDate, endDate, keyword));
    }
}
