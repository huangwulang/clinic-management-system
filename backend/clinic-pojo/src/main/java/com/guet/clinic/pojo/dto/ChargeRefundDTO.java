package com.guet.clinic.pojo.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ChargeRefundDTO {
    private BigDecimal refundAmount;
    private String refundMethod;
}
