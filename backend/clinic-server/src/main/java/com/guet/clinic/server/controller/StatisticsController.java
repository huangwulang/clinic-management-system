package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.entity.Inventory;
import com.guet.clinic.pojo.entity.ChargeOrder;
import com.guet.clinic.pojo.vo.ChargeStatisticsVO;
import com.guet.clinic.pojo.vo.DashboardVO;
import com.guet.clinic.pojo.vo.DrugStatisticsVO;
import com.guet.clinic.pojo.vo.PatientStatisticsVO;
import com.guet.clinic.server.service.ChargeOrderService;
import com.guet.clinic.server.service.DrugService;
import com.guet.clinic.server.service.InventoryService;
import com.guet.clinic.server.service.MemberService;
import com.guet.clinic.server.service.PatientService;
import com.guet.clinic.server.service.RegistrationService;
import com.guet.clinic.server.service.ConsultationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    @Autowired

    private PatientService patientService;
    @Autowired

    private RegistrationService registrationService;
    @Autowired

    private ChargeOrderService chargeOrderService;
    @Autowired
    private DrugService drugService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private ConsultationService consultationService;

    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        LocalDate today = LocalDate.now();
        BigDecimal income = chargeOrderService.list(null).stream()
                .map(order -> {
                    BigDecimal paid = order.getPaidAt() != null
                            && today.equals(order.getPaidAt().toLocalDate())
                            && ("PAID".equals(order.getStatus()) || "REFUNDED".equals(order.getStatus()))
                            ? amount(order.getPaidAmount()) : BigDecimal.ZERO;
                    BigDecimal refund = order.getRefundedAt() != null && today.equals(order.getRefundedAt().toLocalDate())
                            ? amount(order.getRefundAmount()) : BigDecimal.ZERO;
                    return paid.subtract(refund);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        DashboardVO dashboardVO = new DashboardVO(
                patientService.count(null),
                registrationService.count(null),
                drugService.count(null),
                memberService.count(null),
                income
        );
        return Result.success(dashboardVO);
    }

    private BigDecimal amount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @GetMapping("/charge")
    public Result<ChargeStatisticsVO> charge() {
        List<ChargeOrder> orders = chargeOrderService.list(null);
        BigDecimal receivableAmount = orders.stream()
                .map(ChargeOrder::getReceivableAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidAmount = orders.stream()
                .map(ChargeOrder::getPaidAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refundAmount = orders.stream()
                .map(ChargeOrder::getRefundAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ChargeStatisticsVO chargeStatisticsVO = new ChargeStatisticsVO(
                orders.size(),
                orders.stream().filter(order -> "PAID".equals(order.getStatus())).count(),
                orders.stream().filter(order -> "REFUNDED".equals(order.getStatus())).count(),
                receivableAmount,
                paidAmount,
                refundAmount
        );
        return Result.success(chargeStatisticsVO);
    }

    @GetMapping("/patient")
    public Result<PatientStatisticsVO> patient() {
        return Result.success(new PatientStatisticsVO(
                patientService.count(null),
                memberService.count(null),
                registrationService.count(null),
                consultationService.count(null)
        ));
    }

    @GetMapping("/drug")
    public Result<DrugStatisticsVO> drug() {
        List<Inventory> inventories = inventoryService.list(null);
        BigDecimal purchaseAmount = inventories.stream()
                .map(Inventory::getPurchaseAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal retailAmount = inventories.stream()
                .map(Inventory::getRetailAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Result.success(new DrugStatisticsVO(
                drugService.count(null),
                inventories.size(),
                inventoryService.warnings().size(),
                purchaseAmount,
                retailAmount
        ));
    }
}
