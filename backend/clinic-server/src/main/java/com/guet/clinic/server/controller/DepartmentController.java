package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.Department;
import com.guet.clinic.server.service.DepartmentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/departments")
public class DepartmentController extends CrudController<Department> {
    @Autowired
    private DepartmentService departmentService;

    @Override
    protected DepartmentService service() {
        return departmentService;
    }
}
