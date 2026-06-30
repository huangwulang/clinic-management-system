package com.guet.clinic.server.service.impl;

import com.guet.clinic.pojo.entity.OperationLog;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.OperationLogMapper;
import com.guet.clinic.server.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends AbstractCrudService<OperationLog> implements OperationLogService {
    private final OperationLogMapper mapper;

    public OperationLogServiceImpl(OperationLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected CrudMapper<OperationLog> mapper() {
        return mapper;
    }
}
