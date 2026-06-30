package com.guet.clinic.pojo.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeStatisticsVO {
    private long orderCount;
    private long paidCount;
    private long refundedCount;
    private BigDecimal receivableAmount;
    private BigDecimal paidAmount;
    private BigDecimal refundAmount;
}
