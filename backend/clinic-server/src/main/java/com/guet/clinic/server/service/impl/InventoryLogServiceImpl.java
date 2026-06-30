package com.guet.clinic.server.service.impl;

import com.guet.clinic.pojo.entity.InventoryLog;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.InventoryLogMapper;
import com.guet.clinic.server.service.InventoryLogService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InventoryLogServiceImpl extends AbstractCrudService<InventoryLog> implements InventoryLogService {
    private final InventoryLogMapper mapper;
    public InventoryLogServiceImpl(InventoryLogMapper mapper) { this.mapper = mapper; }
    @Override protected CrudMapper<InventoryLog> mapper() { return mapper; }
    @Override public List<InventoryLog> listByInventory(Long inventoryId) { return mapper.selectByInventoryId(inventoryId); }
    @Override public InventoryLog record(Long inventoryId, Long drugId, String type, Integer change, Integer before, Integer after, String remark) {
        InventoryLog log = new InventoryLog();
        log.setInventoryId(inventoryId);
        log.setDrugId(drugId);
        log.setBusinessType(type);
        log.setChangeQuantity(change);
        log.setQuantityBefore(before);
        log.setQuantityAfter(after);
        log.setRemark(remark);
        return save(log);
    }
}
