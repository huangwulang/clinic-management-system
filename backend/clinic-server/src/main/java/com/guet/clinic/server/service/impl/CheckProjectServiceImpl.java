package com.guet.clinic.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.CheckProject;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.CheckProjectMapper;
import com.guet.clinic.server.service.CheckProjectService;
import org.springframework.stereotype.Service;

@Service
public class CheckProjectServiceImpl extends AbstractCrudService<CheckProject> implements CheckProjectService {
    @Autowired
    private CheckProjectMapper checkProjectMapper;

    @Override
    protected CrudMapper<CheckProject> mapper() {
        return checkProjectMapper;
    }

    @Override
    public CheckProject save(CheckProject entity) {
        normalizePriceFields(entity);
        return super.save(entity);
    }

    @Override
    public CheckProject update(Long id, CheckProject entity) {
        normalizePriceFields(entity);
        return super.update(id, entity);
    }

    private void normalizePriceFields(CheckProject entity) {
        if (entity.getRetailPrice() == null && entity.getPrice() != null) {
            entity.setRetailPrice(entity.getPrice());
        }
        if (entity.getPrice() == null && entity.getRetailPrice() != null) {
            entity.setPrice(entity.getRetailPrice());
        }
    }
}
