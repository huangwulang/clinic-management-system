package com.guet.clinic.server.service;

import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.ChargeOrder;

import java.math.BigDecimal;
import java.util.List;

public interface ChargeOrderService extends CrudService<ChargeOrder> {
    PageResult<ChargeOrder> pageByChargeType(int page, int size, String keyword, String chargeType);

    List<ChargeOrder> listByChargeType(String keyword, String chargeType);

    List<ChargeOrder> listByPatientId(Long patientId);

    ChargeOrder saveByChargeType(ChargeOrder chargeOrder, String chargeType);

    ChargeOrder updateByChargeType(Long id, ChargeOrder chargeOrder, String chargeType);

    ChargeOrder pay(Long id, BigDecimal paidAmount, String payMethod, String cashier);

    ChargeOrder refund(Long id, BigDecimal refundAmount, String refundMethod);
}
