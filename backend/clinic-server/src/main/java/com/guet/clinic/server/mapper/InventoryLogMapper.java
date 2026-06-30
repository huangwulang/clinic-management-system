package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.InventoryLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface InventoryLogMapper extends CrudMapper<InventoryLog> {
    List<InventoryLog> selectByInventoryId(@Param("inventoryId") Long inventoryId);
}
