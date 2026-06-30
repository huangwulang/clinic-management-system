package com.guet.clinic.server.config;

import com.guet.clinic.server.interceptor.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private Environment environment;

    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;

    @Bean
    public CorsFilter corsFilter() {
        List<String> allowedOrigins = new ArrayList<>(Binder.get(environment)
                .bind("clinic.cors.allowed-origins", Bindable.listOf(String.class))
                .orElse(List.of("http://localhost:5173", "http://127.0.0.1:5173")));

        // 环境变量追加（逗号分隔），方便 Cloudflare/生产环境注入
        String envOrigins = environment.getProperty("CLINIC_CORS_ORIGINS");
        if (envOrigins != null && !envOrigins.isBlank()) {
            allowedOrigins.addAll(Arrays.asList(envOrigins.split("\\s*,\\s*")));
        }

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // 使用 setAllowedOriginPatterns 支持通配符（Spring 5.3+）
        config.setAllowedOriginPatterns(
                allowedOrigins.stream()
                        .map(origin -> origin.replace(".", "\\.").replace("*", ".*"))
                        .toList()
        );

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/auth/login",
                    "/auth/register",
                    "/auth/sms-code",
                    "/auth/password/code",
                    "/auth/password/reset",
                    "/trial-applications",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                );
    }
}
