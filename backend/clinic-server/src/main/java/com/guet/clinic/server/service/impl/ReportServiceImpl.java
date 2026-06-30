package com.guet.clinic.server.service.impl;

import com.guet.clinic.server.mapper.ReportMapper;
import com.guet.clinic.server.service.ReportService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {
    private final ReportMapper mapper;
    public ReportServiceImpl(ReportMapper mapper) { this.mapper = mapper; }
    @Override public Map<String, Object> dashboardSummary() { return mapper.dashboardSummary(); }
    @Override public List<Map<String, Object>> dashboardTrends(LocalDate start, LocalDate end) { return mapper.dashboardTrends(start, end); }
    @Override public List<Map<String, Object>> dashboardRankings(String type, Integer limit) { return mapper.dashboardRankings(type, limit == null ? 10 : Math.min(Math.max(limit, 1), 50)); }
    @Override public List<Map<String, Object>> workbenchVisits(String status, String keyword, String department, String doctor, LocalDate start, LocalDate end) {
        return mapper.workbenchVisits(status, keyword, department, doctor, start, end);
    }
    @Override public List<Map<String, Object>> chargeStatistics(String type, LocalDate start, LocalDate end, String orderType, String keyword,
                                                               String department, String cashier, String doctor, String tradeType) {
        return mapper.chargeStatistics(type, start, end, orderType, keyword, department, cashier, doctor, tradeType);
    }
    @Override public Map<String, Object> chargeOrderDetail(Long id) { return mapper.chargeOrderDetail(id); }
    @Override public List<Map<String, Object>> patientStatistics(String type, LocalDate start, LocalDate end, String keyword, String doctor, String chargeStatus) {
        return mapper.patientStatistics(type, start, end, keyword, doctor, chargeStatus);
    }
    @Override public List<Map<String, Object>> drugStatistics(String type, LocalDate start, LocalDate end, String keyword,
                                                              String stockInType, String stockOutType, String orderType) {
        return mapper.drugStatistics(type, start, end, keyword, stockInType, stockOutType, orderType);
    }
    @Override public List<Map<String, Object>> checkProjectStatistics(LocalDate start, LocalDate end, String keyword) {
        return mapper.checkProjectStatistics(start, end, keyword);
    }
}
