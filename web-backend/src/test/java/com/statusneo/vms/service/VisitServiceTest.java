package com.statusneo.vms.service;

import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
public class VisitServiceTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");
    @Autowired
    private VisitService visitorService;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Test
    public void testRegisterVisitor() {
        Visitor visitor = new Visitor();
        Visit visit = new Visit();
        visitor.setName("John Doe");
        visitor.setPhoneNumber("1234567890");
        visitor.setEmail("john.doe@example.com");
        visit.setHost("Host Name");
        visitor.setAddress("123 Street, City, Country");
        visit.setVisitDate(LocalDateTime.parse("2022-01-01T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME));

//        Visit registeredVisitor = visitorService.registerVisit(visit);

//        assertNotNull(registeredVisitor);
    }
}

