package com.guet.clinic.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryAdjustDTO {
    @NotNull
    private Integer quantity;

    private String operator;
    private String remark;
}
