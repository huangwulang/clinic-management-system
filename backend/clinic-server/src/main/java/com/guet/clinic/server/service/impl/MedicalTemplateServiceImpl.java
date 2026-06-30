package com.guet.clinic.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.MedicalTemplate;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.MedicalTemplateMapper;
import com.guet.clinic.server.service.MedicalTemplateService;
import org.springframework.stereotype.Service;

@Service
public class MedicalTemplateServiceImpl extends AbstractCrudService<MedicalTemplate> implements MedicalTemplateService {
    @Autowired
    private MedicalTemplateMapper medicalTemplateMapper;

    @Override
    protected CrudMapper<MedicalTemplate> mapper() {
        return medicalTemplateMapper;
    }
}
