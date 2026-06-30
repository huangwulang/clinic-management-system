package com.guet.clinic.server.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guet.clinic.common.constant.BusinessStatusConstant;
import com.guet.clinic.common.exception.BusinessException;
import com.guet.clinic.pojo.dto.ConsultationSectionDTO;
import com.guet.clinic.pojo.entity.ChargeOrder;
import com.guet.clinic.pojo.entity.Consultation;
import com.guet.clinic.server.mapper.ConsultationMapper;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.service.ChargeOrderService;
import com.guet.clinic.server.service.ConsultationService;
import com.guet.clinic.server.service.InventoryService;
import com.guet.clinic.server.service.RegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsultationServiceImpl extends AbstractCrudService<Consultation> implements ConsultationService {
    private final ConsultationMapper consultationMapper;
    private final ChargeOrderService chargeOrderService;
    private final InventoryService inventoryService;
    private final RegistrationService registrationService;
    private final ObjectMapper objectMapper;

    public ConsultationServiceImpl(ConsultationMapper consultationMapper, ChargeOrderService chargeOrderService,
                                   InventoryService inventoryService, RegistrationService registrationService,
                                   ObjectMapper objectMapper) {
        this.consultationMapper = consultationMapper;
        this.chargeOrderService = chargeOrderService;
        this.inventoryService = inventoryService;
        this.registrationService = registrationService;
        this.objectMapper = objectMapper;
    }

    @Override protected CrudMapper<Consultation> mapper() { return consultationMapper; }

    @Override
    public List<Consultation> listByPatientId(Long patientId) {
        return consultationMapper.selectByPatientId(patientId);
    }

    @Override @Transactional
    public Consultation updatePatient(Long id, ConsultationSectionDTO dto) {
        Consultation entity = get(id);
        entity.setPatientId(dto.getPatientId());
        entity.setPatientName(dto.getPatientName());
        entity.setUpdatedAt(LocalDateTime.now());
        consultationMapper.update(entity);
        return get(id);
    }

    @Override @Transactional
    public Consultation saveMedicalRecord(Long id, ConsultationSectionDTO dto) {
        Consultation entity = get(id);
        entity.setDiagnosis(dto.getDiagnosis());
        entity.setDoctorAdvice(dto.getDoctorAdvice());
        entity.setVitalSigns(json(dto.getVitalSign()));
        entity.setMedicalRecord(json(dto.getMedicalRecord()));
        entity.setUpdatedAt(LocalDateTime.now());
        consultationMapper.update(entity);
        return get(id);
    }

    @Override @Transactional
    public Consultation savePrescription(Long id, ConsultationSectionDTO dto) {
        Consultation entity = get(id);
        String nextPrescription = json(dto.getPrescription());
        syncPrescriptionInventory(entity.getPrescription(), nextPrescription);
        entity.setPrescription(nextPrescription);
        entity.setUpdatedAt(LocalDateTime.now());
        consultationMapper.update(entity);
        return get(id);
    }

    @Override @Transactional
    public Consultation clearPrescription(Long id) {
        Consultation entity = get(id);
        syncPrescriptionInventory(entity.getPrescription(), null);
        entity.setPrescription("[]");
        entity.setUpdatedAt(LocalDateTime.now());
        consultationMapper.update(entity);
        return get(id);
    }

    @Override @Transactional
    public Consultation finish(Long id) {
        Consultation entity = get(id);
        entity.setStatus(BusinessStatusConstant.DONE);
        entity.setUpdatedAt(LocalDateTime.now());
        consultationMapper.update(entity);
        if (entity.getRegistrationId() != null) {
            registrationService.complete(entity.getRegistrationId());
        }
        return get(id);
    }

    @Override @Transactional
    public ChargeOrder createChargeOrder(Long id) {
        Consultation consultation = get(id);
        ChargeOrder order = new ChargeOrder();
        order.setOrderNo("SF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + id);
        order.setRegistrationId(consultation.getRegistrationId());
        order.setPatientId(consultation.getPatientId());
        order.setPatientName(consultation.getPatientName());
        order.setChargeType("门诊收费");
        order.setDepartmentName(consultation.getDepartmentName());
        order.setDoctorName(consultation.getDoctorName());
        order.setReceivableAmount(prescriptionAmount(consultation.getPrescription()));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPaidAmount(order.getReceivableAmount());
        order.setRefundAmount(BigDecimal.ZERO);
        order.setPayMethod("\u73b0\u91d1");
        order.setCashier(consultation.getDoctorName());
        order.setStatus(BusinessStatusConstant.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.setRemark(chargePayload(consultation, order.getReceivableAmount()));
        return chargeOrderService.save(order);
    }

    private String chargePayload(Consultation consultation, BigDecimal receivableAmount) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("source", "consultation");
        payload.put("consultationId", consultation.getId());
        payload.put("amount", receivableAmount == null ? "0.00" : receivableAmount.toPlainString());
        JsonNode prescription = parseJson(consultation.getPrescription());

        Map<String, Object> patient = new LinkedHashMap<>();
        JsonNode prescriptionPatient = prescription.path("patient");
        patient.put("code", prescriptionPatient.path("code").asText(consultation.getPatientId() == null ? "" : String.valueOf(consultation.getPatientId())));
        patient.put("name", prescriptionPatient.path("name").asText(consultation.getPatientName()));
        patient.put("age", prescriptionPatient.path("age").asText(""));
        patient.put("gender", prescriptionPatient.path("gender").asText(""));
        patient.put("phone", prescriptionPatient.path("phone").asText(""));
        patient.put("level", prescriptionPatient.path("level").asText("普通"));
        patient.put("balance", prescriptionPatient.path("balance").asText("0.00"));
        patient.put("points", prescriptionPatient.path("points").asText("0"));
        payload.put("patient", patient);

        Map<String, Object> visit = new LinkedHashMap<>();
        visit.put("registrationNo", consultation.getRegistrationId() == null ? "" : String.valueOf(consultation.getRegistrationId()));
        visit.put("department", consultation.getDepartmentName());
        visit.put("doctor", consultation.getDoctorName());
        visit.put("visitDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        visit.put("visitType", consultation.getVisitType());
        visit.put("status", consultation.getStatus());
        payload.put("visit", visit);

        payload.put("tabs", paymentTabs(prescription));
        payload.put("fees", paymentFees(prescription));
        return json(payload);
    }

    private List<Map<String, Object>> paymentTabs(JsonNode prescription) {
        List<Map<String, Object>> tabs = new ArrayList<>();
        Map<String, Object> tab = new LinkedHashMap<>();
        tab.put("key", "prescription");
        tab.put("label", prescription.path("name").asText("处方明细"));
        tab.put("rows", paymentRows(prescription.path("items")));
        tab.put("total", rowsTotal(prescription.path("items")).toPlainString());
        tabs.add(tab);
        return tabs;
    }

    private List<Map<String, Object>> paymentRows(JsonNode items) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!items.isArray()) return rows;
        int index = 1;
        for (JsonNode item : items) {
            BigDecimal price = decimal(item, "price");
            BigDecimal quantity = decimal(item, "quantity");
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                quantity = BigDecimal.ONE;
            }
            BigDecimal amount = price.multiply(quantity);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("index", index++);
            row.put("drugId", item.path("drugId").asLong(item.path("id").asLong(0)));
            row.put("drugCode", item.path("drugCode").asText(item.path("code").asText("")));
            row.put("category", item.path("category").asText(""));
            row.put("type", item.path("type").asText(""));
            row.put("name", item.path("name").asText(""));
            row.put("price", price.toPlainString());
            row.put("quantity", quantity.stripTrailingZeros().toPlainString());
            row.put("unit", item.path("quantityUnit").asText(item.path("unit").asText("")));
            row.put("retailAmount", amount.toPlainString());
            row.put("discount", "10.00");
            row.put("discountAmount", amount.toPlainString());
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> paymentFees(JsonNode prescription) {
        List<Map<String, Object>> rows = new ArrayList<>();
        JsonNode fees = prescription.path("fees");
        if (!fees.isArray()) return rows;
        for (JsonNode fee : fees) {
            BigDecimal amount = decimal(fee, "amount");
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                amount = decimal(fee, "price");
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", fee.path("name").asText("附加费用"));
            row.put("amount", amount.toPlainString());
            rows.add(row);
        }
        return rows;
    }

    private BigDecimal rowsTotal(JsonNode items) {
        BigDecimal total = BigDecimal.ZERO;
        if (!items.isArray()) return total;
        for (JsonNode item : items) {
            BigDecimal quantity = decimal(item, "quantity");
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                quantity = BigDecimal.ONE;
            }
            total = total.add(decimal(item, "price").multiply(quantity));
        }
        return total;
    }

    private void syncPrescriptionInventory(String oldPrescriptionJson, String nextPrescriptionJson) {
        Map<Long, Integer> oldQuantities = prescriptionInventoryQuantities(parseJson(oldPrescriptionJson));
        Map<Long, Integer> nextQuantities = prescriptionInventoryQuantities(parseJson(nextPrescriptionJson));
        for (Map.Entry<Long, Integer> entry : nextQuantities.entrySet()) {
            Long drugId = entry.getKey();
            int delta = entry.getValue() - oldQuantities.getOrDefault(drugId, 0);
            if (delta > 0) {
                inventoryService.decreaseByDrugId(drugId, delta);
            } else if (delta < 0) {
                inventoryService.increaseByDrugId(drugId, -delta);
            }
        }
        for (Map.Entry<Long, Integer> entry : oldQuantities.entrySet()) {
            if (!nextQuantities.containsKey(entry.getKey())) {
                inventoryService.increaseByDrugId(entry.getKey(), entry.getValue());
            }
        }
    }

    private Map<Long, Integer> prescriptionInventoryQuantities(JsonNode prescription) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        JsonNode items = prescription.path("items");
        if (!items.isArray()) return quantities;
        for (JsonNode item : items) {
            if (!isInventoryPrescriptionItem(item)) continue;
            long drugId = item.path("drugId").asLong(item.path("id").asLong(0));
            int quantity = prescriptionQuantity(item);
            if (drugId > 0 && quantity > 0) {
                quantities.merge(drugId, quantity, Integer::sum);
            }
        }
        return quantities;
    }

    private boolean isInventoryPrescriptionItem(JsonNode item) {
        String category = item.path("category").asText("");
        String type = item.path("type").asText("");
        return !category.contains("\u68c0\u67e5") && !type.toLowerCase().contains("check");
    }

    private int prescriptionQuantity(JsonNode item) {
        BigDecimal quantity = decimal(item, "quantity");
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            quantity = BigDecimal.ONE;
        }
        return Math.max(1, quantity.setScale(0, RoundingMode.CEILING).intValue());
    }

    private BigDecimal prescriptionAmount(String prescriptionJson) {
        if (prescriptionJson == null || prescriptionJson.isBlank()) return BigDecimal.ZERO;
        try {
            JsonNode root = objectMapper.readTree(prescriptionJson);
            BigDecimal total = BigDecimal.ZERO;
            JsonNode items = root.path("items");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    BigDecimal price = decimal(item, "price");
                    BigDecimal quantity = decimal(item, "quantity");
                    total = total.add(price.multiply(quantity.compareTo(BigDecimal.ZERO) > 0 ? quantity : BigDecimal.ONE));
                }
            }
            JsonNode fees = root.path("fees");
            if (fees.isArray()) {
                for (JsonNode fee : fees) {
                    BigDecimal amount = decimal(fee, "amount");
                    total = total.add(amount.compareTo(BigDecimal.ZERO) > 0 ? amount : decimal(fee, "price"));
                }
            }
            return total;
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal decimal(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(value.asText("0"));
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private JsonNode parseJson(String value) {
        if (value == null || value.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(value);
        } catch (Exception ex) {
            return objectMapper.createObjectNode();
        }
    }

    private String json(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("JSON参数格式错误");
        }
    }
}
