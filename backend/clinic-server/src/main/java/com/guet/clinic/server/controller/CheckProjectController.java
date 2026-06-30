package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.CheckProject;
import com.guet.clinic.server.service.CheckProjectService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/check-projects")
public class CheckProjectController extends CrudController<CheckProject> {
    @Autowired
    private CheckProjectService checkProjectService;

    @Override
    protected CheckProjectService service() {
        return checkProjectService;
    }
}
