package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.ClinicInfo;
import com.guet.clinic.server.service.ClinicInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/clinic-info", "/system/clinic"})
public class ClinicInfoController extends CrudController<ClinicInfo> {
    @Autowired
    private ClinicInfoService clinicInfoService;

    @Override
    protected ClinicInfoService service() {
        return clinicInfoService;
    }
}
