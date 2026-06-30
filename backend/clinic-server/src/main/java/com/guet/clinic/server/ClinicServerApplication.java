package com.guet.clinic.server;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("com.guet.clinic.server.mapper")
@SpringBootApplication(scanBasePackages = "com.guet.clinic")
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableCaching //开启缓存注解功能
@EnableScheduling //开启任务调度
public class ClinicServerApplication {
    public static void main(String[] args) { SpringApplication.run(ClinicServerApplication.class, args); }
}
