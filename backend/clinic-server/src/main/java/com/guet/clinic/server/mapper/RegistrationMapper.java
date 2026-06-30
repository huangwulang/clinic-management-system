package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RegistrationMapper extends CrudMapper<Registration> {
    Registration selectByRegistrationNo(@Param("registrationNo") String registrationNo);
}
