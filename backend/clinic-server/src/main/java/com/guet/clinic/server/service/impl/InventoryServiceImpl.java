package com.guet.clinic.server.service.impl;

import com.guet.clinic.common.exception.BusinessException;
import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.Inventory;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.InventoryMapper;
import com.guet.clinic.server.service.InventoryLogService;
import com.guet.clinic.server.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class InventoryServiceImpl extends AbstractCrudService<Inventory> implements InventoryService {
    private final InventoryMapper inventoryMapper;
    private final InventoryLogService inventoryLogService;

    public InventoryServiceImpl(InventoryMapper inventoryMapper, InventoryLogService inventoryLogService) {
        this.inventoryMapper = inventoryMapper;
        this.inventoryLogService = inventoryLogService;
    }

    @Override
    protected CrudMapper<Inventory> mapper() {
        return inventoryMapper;
    }

    @Override
    public PageResult<Inventory> search(int page, int size, String keyword, String category) {
        int safePage = Math.max(page, 1);
        int safeSize = size > 0 ? Math.min(size, 200) : 10;
        int offset = (safePage - 1) * safeSize;
        return PageResult.of(
            inventoryMapper.selectPageByFilters(offset, safeSize, keyword, category),
            inventoryMapper.countByFilters(keyword, category),
            safePage,
            safeSize
        );
    }

    @Override
    public Map<String, BigDecimal> summary() {
        return inventoryMapper.selectSummary();
    }

    @Override
    public List<Inventory> warnings() {
        return list(null).stream().filter(item ->
            (item.getWarningStock() != null && item.getQuantity() != null && item.getQuantity() <= item.getWarningStock())
            || (item.getExpireDate() != null && item.getExpireDate().isBefore(LocalDate.now().plusDays(30)))).toList();
    }

    @Override
    @Transactional
    public Inventory increase(Long id, Integer quantity) {
        return change(get(id), validate(quantity), "INCREASE");
    }

    @Override
    @Transactional
    public Inventory decrease(Long id, Integer quantity) {
        return change(get(id), -validate(quantity), "DECREASE");
    }

    @Override
    @Transactional
    public void increaseByDrugId(Long drugId, Integer quantity) {
        int amount = validate(quantity);
        List<Inventory> inventories = inventoryMapper.selectAvailableByDrugId(drugId);
        if (inventories.isEmpty()) {
            throw new BusinessException("\u672a\u627e\u5230\u836f\u54c1\u5e93\u5b58");
        }
        change(inventories.get(0), amount, "PRESCRIPTION_RESTORE");
    }

    @Override
    @Transactional
    public void decreaseByDrugId(Long drugId, Integer quantity) {
        int remaining = validate(quantity);
        List<Inventory> inventories = inventoryMapper.selectAvailableByDrugId(drugId);
        if (inventories.isEmpty()) {
            throw new BusinessException("\u5e93\u5b58\u4e0d\u8db3");
        }
        for (Inventory inventory : inventories) {
            int available = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
            if (available <= 0) {
                continue;
            }
            int amount = Math.min(available, remaining);
            change(inventory, -amount, "PRESCRIPTION_DECREASE");
            remaining -= amount;
            if (remaining == 0) {
                return;
            }
        }
        throw new BusinessException("\u5e93\u5b58\u4e0d\u8db3");
    }

    @Override
    @Transactional
    public void increaseByDrugCode(String drugCode, Integer quantity) {
        int amount = validate(quantity);
        List<Inventory> inventories = inventoryMapper.selectAvailableByDrugCode(normalizeDrugCode(drugCode));
        if (inventories.isEmpty()) {
            throw new BusinessException("\u672a\u627e\u5230\u836f\u54c1\u5e93\u5b58");
        }
        change(inventories.get(0), amount, "RETAIL_RESTORE");
    }

    @Override
    @Transactional
    public void decreaseByDrugCode(String drugCode, Integer quantity) {
        int remaining = validate(quantity);
        List<Inventory> inventories = inventoryMapper.selectAvailableByDrugCode(normalizeDrugCode(drugCode));
        if (inventories.isEmpty()) {
            throw new BusinessException("\u5e93\u5b58\u4e0d\u8db3");
        }
        for (Inventory inventory : inventories) {
            int available = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
            if (available <= 0) {
                continue;
            }
            int amount = Math.min(available, remaining);
            change(inventory, -amount, "SALE");
            remaining -= amount;
            if (remaining == 0) {
                return;
            }
        }
        throw new BusinessException("\u5e93\u5b58\u4e0d\u8db3");
    }

    private Inventory change(Inventory inventory, int change, String type) {
        int before = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
        int after = before + change;
        if (after < 0) {
            throw new BusinessException("\u5e93\u5b58\u4e0d\u8db3");
        }
        inventory.setQuantity(after);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryMapper.update(inventory);
        inventoryLogService.record(inventory.getId(), inventory.getDrugId(), type, change, before, after, "API inventory adjustment");
        return get(inventory.getId());
    }

    private int validate(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("\u6570\u91cf\u5fc5\u987b\u5927\u4e8e0");
        }
        return quantity;
    }

    private String normalizeDrugCode(String drugCode) {
        if (drugCode == null || drugCode.isBlank()) {
            throw new BusinessException("\u836f\u54c1\u7f16\u7801\u4e0d\u80fd\u4e3a\u7a7a");
        }
        return drugCode.trim();
    }
}
