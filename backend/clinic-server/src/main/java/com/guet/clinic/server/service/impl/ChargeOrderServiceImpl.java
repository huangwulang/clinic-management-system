package com.guet.clinic.server.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guet.clinic.common.constant.BusinessStatusConstant;
import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.ChargeOrder;
import com.guet.clinic.server.mapper.ChargeOrderMapper;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.service.ChargeOrderService;
import com.guet.clinic.server.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChargeOrderServiceImpl extends AbstractCrudService<ChargeOrder> implements ChargeOrderService {
    @Autowired
    private ChargeOrderMapper chargeOrderMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected CrudMapper<ChargeOrder> mapper() {
        return chargeOrderMapper;
    }

    @Override
    public PageResult<ChargeOrder> pageByChargeType(int page, int size, String keyword, String chargeType) {
        int safePage = Math.max(page, 1);
        int safeSize = size > 0 ? Math.min(size, 200) : 10;
        int offset = (safePage - 1) * safeSize;
        return PageResult.of(
                chargeOrderMapper.selectPageByChargeType(offset, safeSize, keyword, chargeType),
                chargeOrderMapper.countByChargeType(keyword, chargeType),
                safePage,
                safeSize
        );
    }

    @Override
    public List<ChargeOrder> listByChargeType(String keyword, String chargeType) {
        return chargeOrderMapper.selectPageByChargeType(0, 200, keyword, chargeType);
    }

    @Override
    public List<ChargeOrder> listByPatientId(Long patientId) {
        return chargeOrderMapper.selectByPatientId(patientId);
    }

    @Override
    @Transactional
    public ChargeOrder saveByChargeType(ChargeOrder chargeOrder, String chargeType) {
        chargeOrder.setChargeType(chargeType);
        return save(chargeOrder);
    }

    @Override
    @Transactional
    public ChargeOrder updateByChargeType(Long id, ChargeOrder chargeOrder, String chargeType) {
        chargeOrder.setChargeType(chargeType);
        return update(id, chargeOrder);
    }

    @Override
    @Transactional
    public ChargeOrder pay(Long id, BigDecimal paidAmount, String payMethod, String cashier) {
        ChargeOrder order = get(id);
        order.setPaidAmount(paidAmount == null ? order.getReceivableAmount() : paidAmount);
        order.setPayMethod(payMethod);
        order.setCashier(cashier);
        order.setStatus(BusinessStatusConstant.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        chargeOrderMapper.update(order);
        return get(id);
    }

    @Override
    @Transactional
    public ChargeOrder refund(Long id, BigDecimal refundAmount, String refundMethod) {
        ChargeOrder order = get(id);
        boolean shouldRestoreInventory = !BusinessStatusConstant.REFUNDED.equals(order.getStatus());
        order.setRefundAmount(refundAmount == null ? order.getPaidAmount() : refundAmount);
        order.setRefundMethod(refundMethod);
        order.setStatus(BusinessStatusConstant.REFUNDED);
        order.setRefundedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        chargeOrderMapper.update(order);
        if (shouldRestoreInventory) {
            restoreRefundInventory(order);
        }
        return get(id);
    }

    private void restoreRefundInventory(ChargeOrder order) {
        for (RefundDrugQuantity item : refundDrugQuantities(order.getRemark()).values()) {
            if (item.quantity <= 0) {
                continue;
            }
            if (item.drugId != null && item.drugId > 0) {
                inventoryService.increaseByDrugId(item.drugId, item.quantity);
            } else if (item.drugCode != null && !item.drugCode.isBlank()) {
                inventoryService.increaseByDrugCode(item.drugCode, item.quantity);
            }
        }
    }

    private Map<String, RefundDrugQuantity> refundDrugQuantities(String remark) {
        Map<String, RefundDrugQuantity> quantities = new LinkedHashMap<>();
        if (remark == null || remark.isBlank()) {
            return quantities;
        }
        try {
            JsonNode payload = objectMapper.readTree(remark);
            collectRefundItems(payload.path("items"), quantities);
            JsonNode tabs = payload.path("tabs");
            if (tabs.isArray()) {
                for (JsonNode tab : tabs) {
                    collectRefundItems(tab.path("rows"), quantities);
                }
            }
        } catch (Exception ignored) {
            // Historical free-text remarks do not carry refundable drug details.
        }
        return quantities;
    }

    private void collectRefundItems(JsonNode items, Map<String, RefundDrugQuantity> quantities) {
        if (!items.isArray()) {
            return;
        }
        for (JsonNode item : items) {
            if (!isDrugItem(item)) {
                continue;
            }
            Long drugId = item.path("drugId").asLong(0) > 0 ? item.path("drugId").asLong() : null;
            String drugCode = text(item, "drugCode");
            if (drugCode.isBlank()) {
                drugCode = text(item, "code");
            }
            if (drugId == null && drugCode.isBlank()) {
                continue;
            }
            int quantity = quantity(item);
            if (quantity <= 0) {
                continue;
            }
            String key = drugId != null ? "ID:" + drugId : "CODE:" + drugCode;
            RefundDrugQuantity current = quantities.get(key);
            if (current == null) {
                current = new RefundDrugQuantity(drugId, drugCode);
                quantities.put(key, current);
            }
            current.quantity += quantity;
        }
    }

    private boolean isDrugItem(JsonNode item) {
        String category = text(item, "category");
        String type = text(item, "type").toLowerCase();
        return !category.contains("\u68c0\u67e5") && !type.contains("check");
    }

    private int quantity(JsonNode item) {
        String value = text(item, "quantity");
        if (value.isBlank()) {
            return 1;
        }
        try {
            return Math.max(1, new BigDecimal(value).setScale(0, RoundingMode.CEILING).intValue());
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("");
    }

    private static final class RefundDrugQuantity {
        private final Long drugId;
        private final String drugCode;
        private int quantity;

        private RefundDrugQuantity(Long drugId, String drugCode) {
            this.drugId = drugId;
            this.drugCode = drugCode;
        }
    }
}
