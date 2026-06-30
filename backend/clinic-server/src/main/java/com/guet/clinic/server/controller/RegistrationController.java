package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.StatusUpdateDTO;
import com.guet.clinic.pojo.entity.Registration;
import com.guet.clinic.server.service.RegistrationService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/registrations")
public class RegistrationController extends CrudController<Registration> {
    @Autowired
    private RegistrationService registrationService;

    @Override
    protected RegistrationService service() {
        return registrationService;
    }

    @PostMapping("/{id}/cancel")
    public Result<Registration> cancel(@PathVariable Long id, @RequestBody(required = false) StatusUpdateDTO statusUpdateDTO) {
        return Result.success(registrationService.cancel(id, statusUpdateDTO == null ? null : statusUpdateDTO.getRemark()));
    }

    @PostMapping({"/{id}/start", "/{id}/start-visit"})
    public Result<Registration> start(@PathVariable Long id) {
        return Result.success(registrationService.start(id));
    }

    @PostMapping("/{id}/complete")
    public Result<Registration> complete(@PathVariable Long id) {
        return Result.success(registrationService.complete(id));
    }
}
