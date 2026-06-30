package com.guet.clinic.pojo.vo;

import com.guet.clinic.pojo.entity.InventoryCheck;
import com.guet.clinic.pojo.entity.InventoryCheckItem;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InventoryCheckVO extends InventoryCheck {
    private List<InventoryCheckItem> items;
}
