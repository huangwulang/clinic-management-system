package com.guet.clinic.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.FeeItem;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.FeeItemMapper;
import com.guet.clinic.server.service.FeeItemService;
import org.springframework.stereotype.Service;

@Service
public class FeeItemServiceImpl extends AbstractCrudService<FeeItem> implements FeeItemService {
    @Autowired
    private FeeItemMapper feeItemMapper;

    @Override
    protected CrudMapper<FeeItem> mapper() {
        return feeItemMapper;
    }
}
