package com.statusneo.vms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Configuration
@Slf4j
public class DevProfileConfig {

    @Bean
    public CommandLineRunner devDataLoader() {
        return args -> {
            log.info("🎯 ============================================");
            log.info("🎯 DEV PROFILE ACTIVE - SQLite Mock Data Ready");
            log.info("🎯 Using: dev_visitors.db (SQLite Database)");
            log.info("🎯 MS Graph APIs: MOCKED (No external calls)");
            log.info("🎯 Email Service: MOCKED (Console logging)");
            log.info("🎯 ============================================");
        };
    }
}