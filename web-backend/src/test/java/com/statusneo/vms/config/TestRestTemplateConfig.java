package com.statusneo.vms.config;

import com.statusneo.vms.cache.EmployeeNameCache;
import com.statusneo.vms.controller.VisitorController;
import com.statusneo.vms.repository.EmployeeRepository;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import com.statusneo.vms.service.*;
import com.statusneo.vms.web.EmployeeConverter;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@Configuration
public class TestRestTemplateConfig {
    @TestConfiguration
    static class MockedBeansConfig {
        @Bean
        public EmployeeNameCache employeeNameCache() {
            return mock(EmployeeNameCache.class);
        }

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }


        @Bean
        public GraphDirectoryService graphDirectoryService() {
            return mock(GraphDirectoryService.class);
        }


        @Bean
        public EmailService emailService() {
            return mock(EmailService.class);
        }

        @Bean
        public ExcelService excelService() {
            return mock(ExcelService.class);
        }
    }
}
