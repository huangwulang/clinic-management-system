package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.InventoryAdjustDTO;
import com.guet.clinic.pojo.entity.Inventory;
import com.guet.clinic.pojo.entity.InventoryLog;
import com.guet.clinic.server.service.InventoryLogService;
import com.guet.clinic.server.service.InventoryService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventories")
public class InventoryController extends CrudController<Inventory> {
    private final InventoryService inventoryService;
    private final InventoryLogService inventoryLogService;
    public InventoryController(InventoryService inventoryService, InventoryLogService inventoryLogService) {
        this.inventoryService = inventoryService;
        this.inventoryLogService = inventoryLogService;
    }
    @Override protected InventoryService service() { return inventoryService; }
    @GetMapping("/search")
    public Result<PageResult<Inventory>> search(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.ok(inventoryService.search(page, size, keyword, category));
    }
    @GetMapping("/summary") public Result<Map<String, BigDecimal>> summary() { return Result.ok(inventoryService.summary()); }
    @GetMapping("/warnings") public Result<List<Inventory>> warnings() { return Result.ok(inventoryService.warnings()); }
    @GetMapping("/{id}/logs") public Result<List<InventoryLog>> logs(@PathVariable Long id) {
        inventoryService.get(id);
        return Result.success(inventoryLogService.listByInventory(id));
    }
    @PostMapping("/{id}/increase") public Result<Inventory> increase(@PathVariable Long id, @RequestBody InventoryAdjustDTO dto) {
        return Result.success(inventoryService.increase(id, dto.getQuantity()));
    }
    @PostMapping("/{id}/decrease") public Result<Inventory> decrease(@PathVariable Long id, @RequestBody InventoryAdjustDTO dto) {
        return Result.success(inventoryService.decrease(id, dto.getQuantity()));
    }
}
