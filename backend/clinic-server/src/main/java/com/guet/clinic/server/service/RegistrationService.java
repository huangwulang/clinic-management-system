package com.guet.clinic.server.service;

import com.guet.clinic.pojo.entity.Registration;

public interface RegistrationService extends CrudService<Registration> {
    Registration cancel(Long id, String remark);

    Registration start(Long id);

    Registration complete(Long id);
}
