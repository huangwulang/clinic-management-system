package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.ChargePayDTO;
import com.guet.clinic.pojo.dto.ChargeRefundDTO;
import com.guet.clinic.pojo.entity.ChargeOrder;
import com.guet.clinic.server.service.ChargeOrderService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/charge-orders")
public class ChargeOrderController extends CrudController<ChargeOrder> {
    private final ChargeOrderService service;
    public ChargeOrderController(ChargeOrderService service) { this.service = service; }
    @Override protected ChargeOrderService service() { return service; }
    @PostMapping("/{id}/pay") public Result<ChargeOrder> pay(@PathVariable Long id, @RequestBody ChargePayDTO dto) {
        return Result.success(service.pay(id, dto.getPaidAmount(), dto.getPayMethod(), dto.getCashier()));
    }
    @PostMapping({"/{id}/refund", "/{id}/refunds"}) public Result<ChargeOrder> refund(@PathVariable Long id, @RequestBody ChargeRefundDTO dto) {
        return Result.success(service.refund(id, dto.getRefundAmount(), dto.getRefundMethod()));
    }
    @GetMapping("/{id}/refunds") public Result<List<ChargeOrder>> refunds(@PathVariable Long id) {
        ChargeOrder order = service.get(id);
        return Result.success(order.getRefundAmount() != null && order.getRefundAmount().signum() > 0 ? List.of(order) : List.of());
    }
}
