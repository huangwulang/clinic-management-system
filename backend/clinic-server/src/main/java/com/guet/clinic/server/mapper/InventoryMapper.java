package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface InventoryMapper extends CrudMapper<Inventory> {
    List<Inventory> selectPageByFilters(@Param("offset") int offset,
                                        @Param("size") int size,
                                        @Param("keyword") String keyword,
                                        @Param("category") String category);
    long countByFilters(@Param("keyword") String keyword, @Param("category") String category);
    Map<String, BigDecimal> selectSummary();
    List<Inventory> selectAvailableByDrugId(@Param("drugId") Long drugId);
    List<Inventory> selectAvailableByDrugCode(@Param("drugCode") String drugCode);
}
