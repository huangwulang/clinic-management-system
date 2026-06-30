package com.guet.clinic.server.service;

import com.guet.clinic.pojo.dto.InventoryCheckDTO;
import com.guet.clinic.pojo.entity.InventoryCheck;
import com.guet.clinic.pojo.vo.InventoryCheckVO;

public interface InventoryCheckService extends CrudService<InventoryCheck> {
    String nextCheckNo();
    InventoryCheckVO detail(Long id);
    InventoryCheckVO saveCheck(InventoryCheckDTO dto, boolean complete);
    InventoryCheckVO confirm(Long id);
}
