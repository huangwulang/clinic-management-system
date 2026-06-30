package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.InventoryCheckDTO;
import com.guet.clinic.pojo.entity.InventoryCheck;
import com.guet.clinic.pojo.vo.InventoryCheckVO;
import com.guet.clinic.server.service.InventoryCheckService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/inventory-checks", "/stock-check-orders"})
public class InventoryCheckController extends CrudController<InventoryCheck> {
    private final InventoryCheckService service;

    public InventoryCheckController(InventoryCheckService service) {
        this.service = service;
    }

    @Override
    protected InventoryCheckService service() {
        return service;
    }

    @GetMapping("/next-no")
    public Result<Map<String, String>> nextNo() {
        return Result.ok(Map.of("checkNo", service.nextCheckNo()));
    }

    @GetMapping("/{id}/detail")
    public Result<InventoryCheckVO> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @PostMapping("/draft")
    public Result<InventoryCheckVO> saveDraft(@RequestBody InventoryCheckDTO dto) {
        return Result.ok(service.saveCheck(dto, false));
    }

    @PostMapping("/complete")
    public Result<InventoryCheckVO> complete(@RequestBody InventoryCheckDTO dto) {
        return Result.ok(service.saveCheck(dto, true));
    }

    @PostMapping("/{id}/confirm")
    public Result<InventoryCheckVO> confirm(@PathVariable Long id) {
        return Result.ok(service.confirm(id));
    }
}
