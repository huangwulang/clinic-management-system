package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.StatusUpdateDTO;
import com.guet.clinic.pojo.dto.AuditDTO;
import com.guet.clinic.pojo.entity.StockOrder;
import com.guet.clinic.server.service.StockOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class AbstractStockDirectionController {
    protected abstract StockOrderService stockOrderService();

    protected abstract String stockDirection();

    @GetMapping
    public Result<PageResult<StockOrder>> page(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String stockType,
                                               @RequestParam(required = false) String auditStatus) {
        return Result.ok(stockOrderService().pageByDirection(
                page, size, keyword, stockDirection(), stockType, auditStatus));
    }

    @GetMapping("/list")
    public Result<List<StockOrder>> list(@RequestParam(required = false) String keyword) {
        return Result.ok(stockOrderService().listByDirection(keyword, stockDirection()));
    }

    @GetMapping("/{id}")
    public Result<StockOrder> get(@PathVariable Long id) {
        return Result.ok(stockOrderService().get(id));
    }

    @PostMapping
    public Result<StockOrder> create(@Valid @RequestBody StockOrder stockOrder) {
        return Result.ok(stockOrderService().saveByDirection(stockOrder, stockDirection()));
    }

    @PutMapping("/{id}")
    public Result<StockOrder> update(@PathVariable Long id, @Valid @RequestBody StockOrder stockOrder) {
        return Result.ok(stockOrderService().updateByDirection(id, stockOrder, stockDirection()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stockOrderService().delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/approve")
    public Result<StockOrder> approve(@PathVariable Long id) {
        return Result.success(stockOrderService().approve(id));
    }

    @PostMapping("/{id}/reject")
    public Result<StockOrder> reject(@PathVariable Long id, @RequestBody(required = false) StatusUpdateDTO statusUpdateDTO) {
        return Result.success(stockOrderService().reject(id, statusUpdateDTO == null ? null : statusUpdateDTO.getRemark()));
    }
    @PostMapping("/{id}/submit")
    public Result<StockOrder> submit(@PathVariable Long id) {
        StockOrder order = stockOrderService().get(id);
        order.setAuditStatus("PENDING_AUDIT");
        return Result.success(stockOrderService().updateByDirection(id, order, stockDirection()));
    }

    @PostMapping("/{id}/audit")
    public Result<StockOrder> audit(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        return Result.success(Boolean.TRUE.equals(dto.getApproved())
                ? stockOrderService().approve(id)
                : stockOrderService().reject(id, dto.getRemark()));
    }
}
