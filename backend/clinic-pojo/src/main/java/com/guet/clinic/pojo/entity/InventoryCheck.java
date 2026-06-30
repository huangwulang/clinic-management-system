package com.guet.clinic.pojo.entity;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InventoryCheck extends BaseEntity {
    @NotBlank
    private String checkNo;
    private LocalDateTime checkDate;
    private String maker;
    private String checkStatus;
    private String remark;
}
