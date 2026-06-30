package com.guet.clinic.pojo.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {
    private long patientCount;
    private long registrationCount;
    private long drugCount;
    private long memberCount;
    private BigDecimal income;
}
