package com.guet.clinic.pojo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DictionaryItem extends BaseEntity {
    private String dictType;
    private String itemCode;
    private String itemName;
    private Integer sortNo;
    private Boolean enabled;
    private String remark;
}
