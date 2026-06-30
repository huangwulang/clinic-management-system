package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.StatusUpdateDTO;
import com.guet.clinic.pojo.entity.StockOrder;
import com.guet.clinic.server.service.StockOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock-orders")
public class StockOrderController extends CrudController<StockOrder> {
    @Autowired
    private StockOrderService stockOrderService;

    @Override
    protected StockOrderService service() {
        return stockOrderService;
    }

    @PostMapping("/{id}/approve")
    public Result<StockOrder> approve(@PathVariable Long id) {
        return Result.success(stockOrderService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public Result<StockOrder> reject(@PathVariable Long id, @RequestBody(required = false) StatusUpdateDTO statusUpdateDTO) {
        return Result.success(stockOrderService.reject(id, statusUpdateDTO == null ? null : statusUpdateDTO.getRemark()));
    }
}
