package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.InventoryCheckItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InventoryCheckItemMapper {
    List<InventoryCheckItem> selectByCheckId(@Param("checkId") Long checkId);
    int insert(InventoryCheckItem item);
    int deleteByCheckId(@Param("checkId") Long checkId);
}
