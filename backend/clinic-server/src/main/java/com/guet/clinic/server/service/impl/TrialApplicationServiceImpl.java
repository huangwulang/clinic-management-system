package com.guet.clinic.server.service.impl;

import com.guet.clinic.pojo.entity.TrialApplication;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.TrialApplicationMapper;
import com.guet.clinic.server.service.TrialApplicationService;
import org.springframework.stereotype.Service;

@Service
public class TrialApplicationServiceImpl extends AbstractCrudService<TrialApplication> implements TrialApplicationService {
    private final TrialApplicationMapper mapper;

    public TrialApplicationServiceImpl(TrialApplicationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected CrudMapper<TrialApplication> mapper() {
        return mapper;
    }
}
