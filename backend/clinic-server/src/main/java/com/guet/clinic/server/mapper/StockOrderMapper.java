package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.StockOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockOrderMapper extends CrudMapper<StockOrder> {
    List<StockOrder> selectPageByDirection(@Param("offset") int offset,
                                           @Param("size") int size,
                                           @Param("keyword") String keyword,
                                           @Param("stockDirection") String stockDirection,
                                           @Param("stockType") String stockType,
                                           @Param("auditStatus") String auditStatus);

    long countByDirection(@Param("keyword") String keyword,
                          @Param("stockDirection") String stockDirection,
                          @Param("stockType") String stockType,
                          @Param("auditStatus") String auditStatus);
}
