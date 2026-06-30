package com.guet.clinic.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.Staff;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.StaffMapper;
import com.guet.clinic.server.service.StaffService;
import org.springframework.stereotype.Service;

@Service
public class StaffServiceImpl extends AbstractCrudService<Staff> implements StaffService {
    @Autowired
    private StaffMapper staffMapper;

    @Override
    protected CrudMapper<Staff> mapper() {
        return staffMapper;
    }
}
