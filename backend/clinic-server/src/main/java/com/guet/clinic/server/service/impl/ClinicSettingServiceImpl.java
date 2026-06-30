package com.guet.clinic.server.service.impl;

import com.guet.clinic.pojo.entity.ClinicSetting;
import com.guet.clinic.server.mapper.ClinicSettingMapper;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.service.ClinicSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ClinicSettingServiceImpl extends AbstractCrudService<ClinicSetting> implements ClinicSettingService {
    private final ClinicSettingMapper mapper;
    public ClinicSettingServiceImpl(ClinicSettingMapper mapper) { this.mapper = mapper; }
    @Override protected CrudMapper<ClinicSetting> mapper() { return mapper; }

    @Override public Map<String, String> getSettings() {
        Map<String, String> result = new LinkedHashMap<>();
        list(null).forEach(item -> result.put(item.getSettingKey(), item.getSettingValue()));
        return result;
    }

    @Override @Transactional
    public Map<String, String> saveSettings(Map<String, String> settings) {
        settings.forEach((key, value) -> {
            ClinicSetting existing = mapper.selectByKey(key);
            if (existing == null) {
                ClinicSetting setting = new ClinicSetting();
                setting.setSettingKey(key);
                setting.setSettingValue(value);
                save(setting);
            } else {
                existing.setSettingValue(value);
                update(existing.getId(), existing);
            }
        });
        return getSettings();
    }
}
