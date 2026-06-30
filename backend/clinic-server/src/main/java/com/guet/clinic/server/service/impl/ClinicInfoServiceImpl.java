package com.guet.clinic.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.ClinicInfo;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.ClinicInfoMapper;
import com.guet.clinic.server.service.ClinicInfoService;
import org.springframework.stereotype.Service;

@Service
public class ClinicInfoServiceImpl extends AbstractCrudService<ClinicInfo> implements ClinicInfoService {
    @Autowired
    private ClinicInfoMapper clinicInfoMapper;

    @Override
    protected CrudMapper<ClinicInfo> mapper() {
        return clinicInfoMapper;
    }
}
