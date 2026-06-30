package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.Staff;
import com.guet.clinic.server.service.StaffService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/staff", "/employees"})
public class StaffController extends CrudController<Staff> {
    @Autowired
    private StaffService staffService;

    @Override
    protected StaffService service() {
        return staffService;
    }
}
