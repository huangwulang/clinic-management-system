package com.guet.clinic.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.Supplier;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.SupplierMapper;
import com.guet.clinic.server.service.SupplierService;
import org.springframework.stereotype.Service;

@Service
public class SupplierServiceImpl extends AbstractCrudService<Supplier> implements SupplierService {
    @Autowired
    private SupplierMapper supplierMapper;

    @Override
    protected CrudMapper<Supplier> mapper() {
        return supplierMapper;
    }
}
