package com.guet.clinic.server.service;

import com.guet.clinic.pojo.entity.InventoryLog;
import java.util.List;

public interface InventoryLogService extends CrudService<InventoryLog> {
    List<InventoryLog> listByInventory(Long inventoryId);
    InventoryLog record(Long inventoryId, Long drugId, String type, Integer change, Integer before, Integer after, String remark);
}
