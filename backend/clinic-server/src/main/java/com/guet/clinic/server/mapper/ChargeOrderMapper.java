package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.ChargeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChargeOrderMapper extends CrudMapper<ChargeOrder> {
    List<ChargeOrder> selectPageByChargeType(@Param("offset") int offset,
                                             @Param("size") int size,
                                             @Param("keyword") String keyword,
                                             @Param("chargeType") String chargeType);

    long countByChargeType(@Param("keyword") String keyword,
                           @Param("chargeType") String chargeType);

    List<ChargeOrder> selectByPatientId(@Param("patientId") Long patientId);
}
