package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.Drug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DrugMapper extends CrudMapper<Drug> {
    List<Drug> selectPageByFilters(@Param("offset") int offset,
                                   @Param("size") int size,
                                   @Param("keyword") String keyword,
                                   @Param("category") String category,
                                   @Param("status") String status,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate);
    long countByFilters(@Param("keyword") String keyword,
                        @Param("category") String category,
                        @Param("status") String status,
                        @Param("startDate") String startDate,
                        @Param("endDate") String endDate);
    Drug selectByDrugCode(@Param("drugCode") String drugCode);
    Long selectMaxNumericDrugCode();
    List<Map<String, Object>> selectSaleableDrugs(@Param("keyword") String keyword);
}
