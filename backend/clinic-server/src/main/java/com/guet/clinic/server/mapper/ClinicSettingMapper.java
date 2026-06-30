package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.ClinicSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ClinicSettingMapper extends CrudMapper<ClinicSetting> {
    ClinicSetting selectByKey(@Param("settingKey") String settingKey);
}
