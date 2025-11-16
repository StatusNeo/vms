package com.statusneo.vms.config;
import com.statusneo.vms.service.EmailService;
import com.statusneo.vms.service.GraphDirectoryService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {
    @Bean
    public GraphDirectoryService graphDirectoryService() {
        return mock(GraphDirectoryService.class);
    }
    @Bean
    public EmailService emailService() {
        return mock(EmailService.class);
    }
}