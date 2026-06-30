package com.guet.clinic.pojo.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugStatisticsVO {
    private long drugCount;
    private long inventoryCount;
    private long warningCount;
    private BigDecimal purchaseAmount;
    private BigDecimal retailAmount;
}
