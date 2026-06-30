package com.guet.clinic.server.config;

import com.guet.clinic.common.utils.PasswordUtil;
import com.guet.clinic.pojo.entity.UserAccount;
import com.guet.clinic.server.mapper.UserAccountMapper;
import com.guet.clinic.server.service.UserAccountService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {
    @Bean
    public ApplicationRunner initializeAdmin(UserAccountMapper mapper, UserAccountService service) {
        return args -> {
            UserAccount existing = mapper.selectByUsername("admin");
            if (existing != null) {
                if ((PasswordUtil.matches("123456", existing.getPassword())
                        || PasswordUtil.matchesClientPassword(PasswordUtil.md5("123456"), existing.getPassword()))
                        && !PasswordUtil.encodeClientPassword("123456").equals(existing.getPassword())) {
                    existing.setPassword(PasswordUtil.encodeClientPassword("123456"));
                    existing.setUpdatedAt(LocalDateTime.now());
                    mapper.update(existing);
                }
                return;
            }
            UserAccount account = new UserAccount();
            account.setUsername("admin");
            account.setPassword(PasswordUtil.encodeClientPassword("123456"));
            account.setName("系统管理员");
            account.setRoleName("ADMIN");
            account.setEnabled(true);
            service.save(account);
        };
    }
}
