package com.guet.clinic.server.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {
    Map<String, Object> dashboardSummary();
    List<Map<String, Object>> dashboardTrends(LocalDate startDate, LocalDate endDate);
    List<Map<String, Object>> dashboardRankings(String type, Integer limit);
    List<Map<String, Object>> workbenchVisits(String status, String keyword, String department, String doctor, LocalDate startDate, LocalDate endDate);
    List<Map<String, Object>> chargeStatistics(String type, LocalDate startDate, LocalDate endDate, String orderType, String keyword,
                                               String department, String cashier, String doctor, String tradeType);
    Map<String, Object> chargeOrderDetail(Long id);
    List<Map<String, Object>> patientStatistics(String type, LocalDate startDate, LocalDate endDate, String keyword, String doctor, String chargeStatus);
    List<Map<String, Object>> drugStatistics(String type, LocalDate startDate, LocalDate endDate, String keyword,
                                             String stockInType, String stockOutType, String orderType);
    List<Map<String, Object>> checkProjectStatistics(LocalDate startDate, LocalDate endDate, String keyword);
}
