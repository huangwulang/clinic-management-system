package com.guet.clinic.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.Role;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.RoleMapper;
import com.guet.clinic.server.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends AbstractCrudService<Role> implements RoleService {
    @Autowired
    private RoleMapper roleMapper;

    @Override
    protected CrudMapper<Role> mapper() {
        return roleMapper;
    }
}
