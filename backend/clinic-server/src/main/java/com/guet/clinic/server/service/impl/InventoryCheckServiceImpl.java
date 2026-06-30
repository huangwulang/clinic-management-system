package com.guet.clinic.server.service.impl;

import com.guet.clinic.common.exception.BusinessException;
import com.guet.clinic.pojo.dto.InventoryCheckDTO;
import com.guet.clinic.pojo.entity.Inventory;
import com.guet.clinic.pojo.entity.InventoryCheck;
import com.guet.clinic.pojo.entity.InventoryCheckItem;
import com.guet.clinic.pojo.vo.InventoryCheckVO;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.InventoryCheckItemMapper;
import com.guet.clinic.server.mapper.InventoryCheckMapper;
import com.guet.clinic.server.mapper.InventoryMapper;
import com.guet.clinic.server.service.InventoryCheckService;
import com.guet.clinic.server.service.InventoryLogService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryCheckServiceImpl extends AbstractCrudService<InventoryCheck> implements InventoryCheckService {
    private static final DateTimeFormatter NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final InventoryCheckMapper inventoryCheckMapper;
    private final InventoryCheckItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryLogService inventoryLogService;

    public InventoryCheckServiceImpl(InventoryCheckMapper inventoryCheckMapper,
                                     InventoryCheckItemMapper itemMapper,
                                     InventoryMapper inventoryMapper,
                                     InventoryLogService inventoryLogService) {
        this.inventoryCheckMapper = inventoryCheckMapper;
        this.itemMapper = itemMapper;
        this.inventoryMapper = inventoryMapper;
        this.inventoryLogService = inventoryLogService;
    }

    @Override
    protected CrudMapper<InventoryCheck> mapper() {
        return inventoryCheckMapper;
    }

    @Override
    public String nextCheckNo() {
        return "PD" + LocalDateTime.now().format(NO_FORMAT);
    }

    @Override
    @Transactional
    public InventoryCheckVO detail(Long id) {
        InventoryCheck check = get(id);
        List<InventoryCheckItem> items = itemMapper.selectByCheckId(id);
        if (items.isEmpty()) {
            items = snapshotCurrentInventory(id);
        }
        enrichPrescriptionCategories(items);
        return toVO(check, items);
    }

    @Override
    @Transactional
    public InventoryCheckVO saveCheck(InventoryCheckDTO dto, boolean complete) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("盘点明细不能为空");
        }
        if (complete && dto.getItems().stream().anyMatch(item -> item.getCheckQuantity() == null)) {
            throw new BusinessException("完成盘点前请填写全部实盘库存");
        }

        LocalDateTime now = LocalDateTime.now();
        InventoryCheck check = new InventoryCheck();
        check.setCheckNo(dto.getCheckNo() == null || dto.getCheckNo().isBlank() ? nextCheckNo() : dto.getCheckNo());
        check.setCheckDate(now);
        check.setMaker(dto.getMaker());
        check.setCheckStatus(complete ? "CONFIRMED" : "DRAFT");
        check.setRemark(dto.getRemark());
        check.setCreatedAt(now);
        check.setUpdatedAt(now);
        check.setDeleted(false);
        inventoryCheckMapper.insert(check);

        List<InventoryCheckItem> savedItems = new ArrayList<>();
        for (InventoryCheckItem source : dto.getItems()) {
            Inventory inventory = inventoryMapper.selectById(source.getInventoryId());
            if (inventory == null) {
                throw new BusinessException("库存数据不存在或已删除");
            }
            InventoryCheckItem item = buildItem(check.getId(), inventory, source.getCheckQuantity(), source.getRemark(), now);
            itemMapper.insert(item);
            savedItems.add(item);
        }
        if (complete) {
            applyInventory(check, savedItems);
        }
        return toVO(check, savedItems);
    }

    @Override
    @Transactional
    public InventoryCheckVO confirm(Long id) {
        InventoryCheck check = get(id);
        if ("CONFIRMED".equalsIgnoreCase(check.getCheckStatus())) {
            return detail(id);
        }
        List<InventoryCheckItem> items = itemMapper.selectByCheckId(id);
        if (items.isEmpty()) {
            items = snapshotCurrentInventory(id);
        }
        if (items.stream().anyMatch(item -> item.getCheckQuantity() == null)) {
            throw new BusinessException("完成盘点前请填写全部实盘库存");
        }
        applyInventory(check, items);
        InventoryCheck update = new InventoryCheck();
        update.setId(id);
        update.setCheckStatus("CONFIRMED");
        update.setCheckDate(LocalDateTime.now());
        update.setUpdatedAt(LocalDateTime.now());
        inventoryCheckMapper.update(update);
        return detail(id);
    }

    private void enrichPrescriptionCategories(List<InventoryCheckItem> items) {
        for (InventoryCheckItem item : items) {
            if (item.getPrescriptionCategory() != null && !item.getPrescriptionCategory().isBlank()) {
                continue;
            }
            Inventory inventory = inventoryMapper.selectById(item.getInventoryId());
            if (inventory != null) {
                item.setPrescriptionCategory(inventory.getPrescriptionCategory());
            }
        }
    }

    private List<InventoryCheckItem> snapshotCurrentInventory(Long checkId) {
        List<Inventory> inventories = inventoryMapper.selectPageByFilters(0, 200, null, null);
        LocalDateTime now = LocalDateTime.now();
        List<InventoryCheckItem> items = new ArrayList<>();
        for (Inventory inventory : inventories) {
            InventoryCheckItem item = buildItem(
                checkId,
                inventory,
                inventory.getQuantity(),
                null,
                now
            );
            itemMapper.insert(item);
            items.add(item);
        }
        return items;
    }

    private InventoryCheckItem buildItem(Long checkId, Inventory inventory, Integer checkQuantity,
                                         String remark, LocalDateTime now) {
        if (checkQuantity != null && checkQuantity < 0) {
            throw new BusinessException("实盘库存不能小于0");
        }
        InventoryCheckItem item = new InventoryCheckItem();
        item.setCheckId(checkId);
        item.setInventoryId(inventory.getId());
        item.setDrugId(inventory.getDrugId());
        item.setDrugCode(inventory.getDrugCode());
        item.setDrugName(inventory.getDrugName());
        item.setLocationNo(inventory.getLocationNo());
        item.setPrescriptionCategory(inventory.getPrescriptionCategory());
        item.setSpecification(inventory.getSpecification());
        item.setManufacturer(inventory.getManufacturer());
        item.setSystemQuantity(inventory.getQuantity());
        item.setCheckQuantity(checkQuantity);
        item.setDifferenceQuantity(checkQuantity == null ? null : checkQuantity - inventory.getQuantity());
        item.setUnit(inventory.getUnit());
        item.setRemark(remark);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        item.setDeleted(false);
        return item;
    }

    private void applyInventory(InventoryCheck check, List<InventoryCheckItem> items) {
        for (InventoryCheckItem item : items) {
            Inventory inventory = inventoryMapper.selectById(item.getInventoryId());
            if (inventory == null || item.getCheckQuantity() == null) {
                continue;
            }
            int before = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
            int after = item.getCheckQuantity();
            int change = after - before;
            if (change == 0) {
                continue;
            }
            Inventory update = new Inventory();
            update.setId(inventory.getId());
            update.setQuantity(after);
            update.setPurchaseAmount(calculateAmount(inventory.getPurchaseAmount(), before, after));
            update.setRetailAmount(calculateAmount(inventory.getRetailAmount(), before, after));
            update.setUpdatedAt(LocalDateTime.now());
            inventoryMapper.update(update);
            inventoryLogService.record(
                inventory.getId(),
                inventory.getDrugId(),
                "CHECK_ADJUST",
                change,
                before,
                after,
                "库存盘点 " + check.getCheckNo()
            );
        }
    }

    private java.math.BigDecimal calculateAmount(java.math.BigDecimal amount, int before, int after) {
        if (amount == null || before <= 0) {
            return amount == null ? java.math.BigDecimal.ZERO : amount;
        }
        return amount.divide(java.math.BigDecimal.valueOf(before), 4, java.math.RoundingMode.HALF_UP)
            .multiply(java.math.BigDecimal.valueOf(after))
            .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private InventoryCheckVO toVO(InventoryCheck check, List<InventoryCheckItem> items) {
        InventoryCheckVO vo = new InventoryCheckVO();
        vo.setId(check.getId());
        vo.setCheckNo(check.getCheckNo());
        vo.setCheckDate(check.getCheckDate());
        vo.setMaker(check.getMaker());
        vo.setCheckStatus(check.getCheckStatus());
        vo.setRemark(check.getRemark());
        vo.setCreatedAt(check.getCreatedAt());
        vo.setUpdatedAt(check.getUpdatedAt());
        vo.setDeleted(check.getDeleted());
        vo.setItems(items);
        return vo;
    }
}
