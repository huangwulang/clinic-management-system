package com.guet.clinic.server.service;

import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.Inventory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface InventoryService extends CrudService<Inventory> {
    PageResult<Inventory> search(int page, int size, String keyword, String category);
    List<Inventory> warnings();
    Map<String, BigDecimal> summary();
    Inventory increase(Long id, Integer quantity);
    Inventory decrease(Long id, Integer quantity);
    void increaseByDrugId(Long drugId, Integer quantity);
    void decreaseByDrugId(Long drugId, Integer quantity);
    void increaseByDrugCode(String drugCode, Integer quantity);
    void decreaseByDrugCode(String drugCode, Integer quantity);
}
