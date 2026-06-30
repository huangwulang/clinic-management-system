package com.guet.clinic.server.service.impl;

import com.guet.clinic.pojo.entity.SystemMessage;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.SystemMessageMapper;
import com.guet.clinic.server.service.SystemMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemMessageServiceImpl extends AbstractCrudService<SystemMessage> implements SystemMessageService {
    private final SystemMessageMapper mapper;

    public SystemMessageServiceImpl(SystemMessageMapper mapper) {
        this.mapper = mapper;
    }

    @Override protected CrudMapper<SystemMessage> mapper() { return mapper; }

    @Override
    @Transactional
    public SystemMessage markRead(Long id) {
        get(id);
        mapper.markRead(id);
        return get(id);
    }

    @Override
    @Transactional
    public void markAllRead(Long receiverId) {
        mapper.markAllRead(receiverId);
    }
}
