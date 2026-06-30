package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.PriceAdjustment;
import com.guet.clinic.pojo.vo.PriceAdjustmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PriceAdjustmentMapper extends CrudMapper<PriceAdjustment> {
    List<PriceAdjustmentVO> selectAdjustableDrugs(@Param("offset") int offset,
                                                  @Param("size") int size,
                                                  @Param("keyword") String keyword,
                                                  @Param("category") String category);
    long countAdjustableDrugs(@Param("keyword") String keyword, @Param("category") String category);
    List<PriceAdjustmentVO> selectRecordSummaries(@Param("offset") int offset,
                                                  @Param("size") int size,
                                                  @Param("keyword") String keyword,
                                                  @Param("category") String category);
    long countRecordSummaries(@Param("keyword") String keyword, @Param("category") String category);
    List<PriceAdjustmentVO> selectFormRows(@Param("drugId") Long drugId);
    List<PriceAdjustmentVO> selectHistoryByDrugId(@Param("drugId") Long drugId);
    int updateDrugPrices(@Param("drugId") Long drugId,
                         @Param("purchasePrice") BigDecimal purchasePrice,
                         @Param("sellPrice") BigDecimal sellPrice,
                         @Param("updatedAt") LocalDateTime updatedAt);
    int updateInventoryAmounts(@Param("drugId") Long drugId,
                               @Param("purchasePrice") BigDecimal purchasePrice,
                               @Param("sellPrice") BigDecimal sellPrice,
                               @Param("updatedAt") LocalDateTime updatedAt);
}
