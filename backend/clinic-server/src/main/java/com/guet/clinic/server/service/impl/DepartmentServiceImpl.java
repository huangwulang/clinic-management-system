package com.guet.clinic.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.Department;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.DepartmentMapper;
import com.guet.clinic.server.service.DepartmentService;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl extends AbstractCrudService<Department> implements DepartmentService {
    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    protected CrudMapper<Department> mapper() {
        return departmentMapper;
    }
}
