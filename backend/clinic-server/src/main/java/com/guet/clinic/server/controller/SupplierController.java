package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.Supplier;
import com.guet.clinic.server.service.SupplierService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/suppliers")
public class SupplierController extends CrudController<Supplier> {
    @Autowired
    private SupplierService supplierService;

    @Override
    protected SupplierService service() {
        return supplierService;
    }
}
