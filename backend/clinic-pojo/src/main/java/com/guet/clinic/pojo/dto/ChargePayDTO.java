package com.guet.clinic.pojo.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ChargePayDTO {
    private BigDecimal paidAmount;
    private String payMethod;
    private String cashier;
}
