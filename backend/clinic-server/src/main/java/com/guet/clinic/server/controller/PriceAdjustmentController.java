package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.entity.PriceAdjustment;
import com.guet.clinic.pojo.vo.PriceAdjustmentVO;
import com.guet.clinic.server.service.PriceAdjustmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/price-adjustments", "/price-adjust-orders"})
public class PriceAdjustmentController extends CrudController<PriceAdjustment> {
    private final PriceAdjustmentService service;

    public PriceAdjustmentController(PriceAdjustmentService service) {
        this.service = service;
    }

    @Override
    protected PriceAdjustmentService service() {
        return service;
    }

    @GetMapping("/adjustable-drugs")
    public Result<PageResult<PriceAdjustmentVO>> adjustableDrugs(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) String category) {
        return Result.ok(service.adjustableDrugs(page, size, keyword, category));
    }

    @GetMapping("/records")
    public Result<PageResult<PriceAdjustmentVO>> records(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) String category) {
        return Result.ok(service.recordSummaries(page, size, keyword, category));
    }

    @GetMapping("/drugs/{drugId}/form")
    public Result<List<PriceAdjustmentVO>> form(@PathVariable Long drugId) {
        return Result.ok(service.formRows(drugId));
    }

    @GetMapping("/drugs/{drugId}/history")
    public Result<List<PriceAdjustmentVO>> history(@PathVariable Long drugId) {
        return Result.ok(service.history(drugId));
    }

    @PostMapping("/complete")
    public Result<PriceAdjustment> complete(@RequestBody PriceAdjustment request) {
        return Result.ok(service.complete(request));
    }
}
