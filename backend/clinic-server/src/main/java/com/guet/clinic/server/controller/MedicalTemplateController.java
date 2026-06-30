package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.MedicalTemplate;
import com.guet.clinic.server.service.MedicalTemplateService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medical-templates")
public class MedicalTemplateController extends CrudController<MedicalTemplate> {
    @Autowired
    private MedicalTemplateService medicalTemplateService;

    @Override
    protected MedicalTemplateService service() {
        return medicalTemplateService;
    }
}
