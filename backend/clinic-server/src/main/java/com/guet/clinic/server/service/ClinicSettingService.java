package com.guet.clinic.server.service;

import com.guet.clinic.pojo.entity.ClinicSetting;
import java.util.Map;

public interface ClinicSettingService extends CrudService<ClinicSetting> {
    Map<String, String> getSettings();
    Map<String, String> saveSettings(Map<String, String> settings);
}
