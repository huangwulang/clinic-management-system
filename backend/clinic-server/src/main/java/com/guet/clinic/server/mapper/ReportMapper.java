package com.guet.clinic.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {
    Map<String, Object> dashboardSummary();
    List<Map<String, Object>> dashboardTrends(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    List<Map<String, Object>> dashboardRankings(@Param("type") String type, @Param("limit") int limit);
    List<Map<String, Object>> workbenchVisits(@Param("status") String status, @Param("keyword") String keyword,
                                               @Param("department") String department, @Param("doctor") String doctor,
                                               @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    List<Map<String, Object>> chargeStatistics(@Param("type") String type, @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("orderType") String orderType,
                                                @Param("keyword") String keyword,
                                                @Param("department") String department,
                                                @Param("cashier") String cashier,
                                                @Param("doctor") String doctor,
                                                @Param("tradeType") String tradeType);
    Map<String, Object> chargeOrderDetail(@Param("id") Long id);
    List<Map<String, Object>> patientStatistics(@Param("type") String type, @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("keyword") String keyword,
                                                 @Param("doctor") String doctor,
                                                 @Param("chargeStatus") String chargeStatus);
    List<Map<String, Object>> drugStatistics(@Param("type") String type, @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("keyword") String keyword,
                                              @Param("stockInType") String stockInType,
                                              @Param("stockOutType") String stockOutType,
                                              @Param("orderType") String orderType);
    List<Map<String, Object>> checkProjectStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                                                      @Param("keyword") String keyword);
}
