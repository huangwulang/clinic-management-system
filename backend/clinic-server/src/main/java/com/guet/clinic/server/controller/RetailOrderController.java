package com.guet.clinic.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guet.clinic.common.constant.BusinessStatusConstant;
import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.ChargePayDTO;
import com.guet.clinic.pojo.dto.ChargeRefundDTO;
import com.guet.clinic.pojo.entity.ChargeOrder;
import com.guet.clinic.server.service.ChargeOrderService;
import com.guet.clinic.server.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/retail-orders")
public class RetailOrderController {
    private static final String CHARGE_TYPE_RETAIL = "DRUG_RETAIL";

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public Result<PageResult<ChargeOrder>> page(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String keyword) {
        return Result.ok(chargeOrderService.pageByChargeType(page, size, keyword, CHARGE_TYPE_RETAIL));
    }

    @GetMapping("/list")
    public Result<List<ChargeOrder>> list(@RequestParam(required = false) String keyword) {
        return Result.ok(chargeOrderService.listByChargeType(keyword, CHARGE_TYPE_RETAIL));
    }

    @GetMapping("/rankings")
    public Result<List<Map<String, Object>>> rankings(@RequestParam(defaultValue = "10") int limit) {
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        Map<String, RetailRankItem> rankMap = new LinkedHashMap<>();

        for (ChargeOrder order : chargeOrderService.listByChargeType(null, CHARGE_TYPE_RETAIL)) {
            LocalDateTime orderTime = order.getPaidAt() != null ? order.getPaidAt() : order.getCreatedAt();
            if (orderTime != null && orderTime.toLocalDate().isBefore(weekStart)) {
                continue;
            }
            mergeRetailItems(rankMap, order.getRemark());
        }

        int safeLimit = Math.min(Math.max(limit, 1), 10);
        List<Map<String, Object>> rows = rankMap.values().stream()
                .sorted(Comparator.comparing(RetailRankItem::getQuantity).reversed()
                        .thenComparing(RetailRankItem::getAmount, Comparator.reverseOrder()))
                .limit(safeLimit)
                .map(RetailRankItem::toMap)
                .toList();
        return Result.ok(rows);
    }

    @GetMapping("/{id}")
    public Result<ChargeOrder> get(@PathVariable Long id) {
        return Result.ok(chargeOrderService.get(id));
    }

    @PostMapping
    @Transactional
    public Result<ChargeOrder> create(@Valid @RequestBody ChargeOrder chargeOrder) {
        ChargeOrder saved = chargeOrderService.saveByChargeType(chargeOrder, CHARGE_TYPE_RETAIL);
        syncRetailInventory(null, saved);
        return Result.ok(saved);
    }

    @PutMapping("/{id}")
    @Transactional
    public Result<ChargeOrder> update(@PathVariable Long id, @Valid @RequestBody ChargeOrder chargeOrder) {
        ChargeOrder oldOrder = chargeOrderService.get(id);
        ChargeOrder saved = chargeOrderService.updateByChargeType(id, chargeOrder, CHARGE_TYPE_RETAIL);
        syncRetailInventory(oldOrder, saved);
        return Result.ok(saved);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        chargeOrderService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/pay")
    @Transactional
    public Result<ChargeOrder> pay(@PathVariable Long id, @RequestBody ChargePayDTO chargePayDTO) {
        ChargeOrder oldOrder = chargeOrderService.get(id);
        ChargeOrder paidOrder = chargeOrderService.pay(id, chargePayDTO.getPaidAmount(), chargePayDTO.getPayMethod(), chargePayDTO.getCashier());
        syncRetailInventory(oldOrder, paidOrder);
        return Result.success(paidOrder);
    }

    @PostMapping("/{id}/refund")
    public Result<ChargeOrder> refund(@PathVariable Long id, @RequestBody ChargeRefundDTO chargeRefundDTO) {
        return Result.success(chargeOrderService.refund(id, chargeRefundDTO.getRefundAmount(), chargeRefundDTO.getRefundMethod()));
    }

    private void syncRetailInventory(ChargeOrder oldOrder, ChargeOrder nextOrder) {
        Map<String, Integer> oldQuantities = retailQuantities(oldOrder);
        Map<String, Integer> nextQuantities = retailQuantities(nextOrder);
        for (Map.Entry<String, Integer> entry : nextQuantities.entrySet()) {
            String drugCode = entry.getKey();
            int delta = entry.getValue() - oldQuantities.getOrDefault(drugCode, 0);
            if (delta > 0) {
                inventoryService.decreaseByDrugCode(drugCode, delta);
            } else if (delta < 0) {
                inventoryService.increaseByDrugCode(drugCode, -delta);
            }
        }
        for (Map.Entry<String, Integer> entry : oldQuantities.entrySet()) {
            if (!nextQuantities.containsKey(entry.getKey())) {
                inventoryService.increaseByDrugCode(entry.getKey(), entry.getValue());
            }
        }
    }

    private Map<String, Integer> retailQuantities(ChargeOrder order) {
        Map<String, Integer> quantities = new LinkedHashMap<>();
        if (order == null || !BusinessStatusConstant.PAID.equals(order.getStatus())
                || order.getRemark() == null || order.getRemark().isBlank()) {
            return quantities;
        }
        try {
            JsonNode items = objectMapper.readTree(order.getRemark()).path("items");
            if (!items.isArray()) {
                return quantities;
            }
            for (JsonNode item : items) {
                String code = text(item, "code");
                int quantity = decimal(item, "quantity").setScale(0, RoundingMode.CEILING).intValue();
                if (!code.isBlank() && quantity > 0) {
                    quantities.merge(code, quantity, Integer::sum);
                }
            }
        } catch (Exception ignored) {
            // Malformed retail payloads should not break historical order reads.
        }
        return quantities;
    }

    private void mergeRetailItems(Map<String, RetailRankItem> rankMap, String remark) {
        if (remark == null || remark.isBlank()) {
            return;
        }
        try {
            JsonNode items = objectMapper.readTree(remark).path("items");
            if (!items.isArray()) {
                return;
            }
            for (JsonNode item : items) {
                String name = text(item, "name");
                if (name.isBlank()) {
                    continue;
                }
                String code = text(item, "code");
                String key = code.isBlank() ? name : code;
                BigDecimal quantity = decimal(item, "quantity");
                BigDecimal amount = decimal(item, "retailAmount");
                if (amount.compareTo(BigDecimal.ZERO) == 0) {
                    amount = decimal(item, "discountAmount");
                }
                RetailRankItem rank = rankMap.computeIfAbsent(key, ignored -> new RetailRankItem(code, name));
                rank.spec = firstNotBlank(rank.spec, text(item, "spec"));
                rank.unit = firstNotBlank(rank.unit, text(item, "unit"));
                rank.quantity = rank.quantity.add(quantity);
                rank.amount = rank.amount.add(amount);
            }
        } catch (Exception ignored) {
            // Ignore malformed historical remarks instead of failing the ranking page.
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("");
    }

    private BigDecimal decimal(JsonNode node, String field) {
        String value = text(node, field);
        if (value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            return BigDecimal.ZERO;
        }
    }

    private String firstNotBlank(String current, String next) {
        return current == null || current.isBlank() ? next : current;
    }

    private static final class RetailRankItem {
        private final String code;
        private final String name;
        private String spec = "";
        private String unit = "";
        private BigDecimal quantity = BigDecimal.ZERO;
        private BigDecimal amount = BigDecimal.ZERO;

        private RetailRankItem(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private BigDecimal getQuantity() {
            return quantity;
        }

        private BigDecimal getAmount() {
            return amount;
        }

        private Map<String, Object> toMap() {
            BigDecimal price = quantity.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(quantity, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            return Map.of(
                    "code", code == null || code.isBlank() ? name : code,
                    "name", name,
                    "spec", spec == null ? "" : spec,
                    "quantity", quantity,
                    "unit", unit == null ? "" : unit,
                    "price", price,
                    "amount", amount
            );
        }
    }
}
