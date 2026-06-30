package com.guet.clinic.server.service.impl;

import com.guet.clinic.common.constant.BusinessStatusConstant;
import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.StockOrder;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.StockOrderMapper;
import com.guet.clinic.server.service.StockOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockOrderServiceImpl extends AbstractCrudService<StockOrder> implements StockOrderService {
    @Autowired
    private StockOrderMapper stockOrderMapper;

    @Override
    protected CrudMapper<StockOrder> mapper() {
        return stockOrderMapper;
    }

    @Override
    public PageResult<StockOrder> pageByDirection(int page, int size, String keyword,
                                                  String stockDirection, String stockType,
                                                  String auditStatus) {
        int safePage = Math.max(page, 1);
        int safeSize = size > 0 ? Math.min(size, 200) : 10;
        int offset = (safePage - 1) * safeSize;
        return PageResult.of(
                stockOrderMapper.selectPageByDirection(
                        offset, safeSize, keyword, stockDirection, stockType, auditStatus),
                stockOrderMapper.countByDirection(
                        keyword, stockDirection, stockType, auditStatus),
                safePage,
                safeSize
        );
    }

    @Override
    public List<StockOrder> listByDirection(String keyword, String stockDirection) {
        return stockOrderMapper.selectPageByDirection(
                0, 200, keyword, stockDirection, null, null);
    }

    @Override
    @Transactional
    public StockOrder saveByDirection(StockOrder stockOrder, String stockDirection) {
        stockOrder.setStockDirection(stockDirection);
        return save(stockOrder);
    }

    @Override
    @Transactional
    public StockOrder updateByDirection(Long id, StockOrder stockOrder, String stockDirection) {
        stockOrder.setStockDirection(stockDirection);
        return update(id, stockOrder);
    }

    @Override
    @Transactional
    public StockOrder approve(Long id) {
        StockOrder order = get(id);
        order.setAuditStatus(BusinessStatusConstant.AUDIT_APPROVED);
        order.setAuditAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        stockOrderMapper.update(order);
        return get(id);
    }

    @Override
    @Transactional
    public StockOrder reject(Long id, String remark) {
        StockOrder order = get(id);
        order.setAuditStatus(BusinessStatusConstant.AUDIT_REJECTED);
        order.setAuditAt(LocalDateTime.now());
        order.setRemark(remark == null ? order.getRemark() : remark);
        order.setUpdatedAt(LocalDateTime.now());
        stockOrderMapper.update(order);
        return get(id);
    }
}
