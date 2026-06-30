package com.guet.clinic.server.service;

import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.StockOrder;

import java.util.List;

public interface StockOrderService extends CrudService<StockOrder> {
    PageResult<StockOrder> pageByDirection(int page, int size, String keyword, String stockDirection,
                                           String stockType, String auditStatus);

    List<StockOrder> listByDirection(String keyword, String stockDirection);

    StockOrder saveByDirection(StockOrder stockOrder, String stockDirection);

    StockOrder updateByDirection(Long id, StockOrder stockOrder, String stockDirection);

    StockOrder approve(Long id);

    StockOrder reject(Long id, String remark);
}
