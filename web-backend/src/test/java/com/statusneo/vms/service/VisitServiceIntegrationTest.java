
/*
 * Copyright [2025] StatusNeo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: StatusNeo
 */
package com.statusneo.vms.service;

import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers

public class VisitServiceIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private VisitService visitService;

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private VisitRepository visitRepository;

    @BeforeEach
    void cleanDatabase() {
        visitRepository.deleteAll();
        visitorRepository.deleteAll();
    }

    @Test
    void testRegisterVisitCreatesVisitorAndVisit() {
        Visitor visitor = new Visitor();
        visitor.setName("Alice");
        visitor.setPhoneNumber("1234567890");
        visitor.setEmail("alice@example.com");
        visitor.setAddress("123 Main St");

        Visit savedVisit = visitService.registerVisit(visitor);

        assertNotNull(savedVisit);
        assertNotNull(savedVisit.getId());
        assertNotNull(savedVisit.getVisitor());
        assertEquals("Alice", savedVisit.getVisitor().getName());

        // Check that visitor is persisted
        Optional<Visitor> foundVisitor = visitorRepository.findById(savedVisit.getVisitor().getId());
        assertTrue(foundVisitor.isPresent());
        assertEquals("alice@example.com", foundVisitor.get().getEmail());

        // Check that visit is persisted
        Optional<Visit> foundVisit = visitRepository.findById(savedVisit.getId());
        assertTrue(foundVisit.isPresent());
        assertEquals(savedVisit.getId(), foundVisit.get().getId());
    }

    @Test
    void testRegisterVisitWithExistingVisitor() {
        Visitor visitor = new Visitor();
        visitor.setName("Bob");
        visitor.setPhoneNumber("9876543210");
        visitor.setEmail("bob@example.com");
        visitor.setAddress("456 Elm St");
        Visitor persistedVisitor = visitorRepository.save(visitor);

        Visit visit = visitService.registerVisit(persistedVisitor);

        assertNotNull(visit);
        assertEquals("Bob", visit.getVisitor().getName());
        assertEquals("bob@example.com", visit.getVisitor().getEmail());
    }

    @Test
    void testVisitDateIsSetToNow() {
        Visitor visitor = new Visitor();
        visitor.setName("Charlie");
        visitor.setPhoneNumber("1112223333");
        visitor.setEmail("charlie@example.com");
        visitor.setAddress("789 Oak St");

        Visit visit = visitService.registerVisit(visitor);

        assertNotNull(visit.getVisitDate());
        assertTrue(visit.getVisitDate().isAfter(LocalDateTime.now().minusMinutes(1)));
        assertTrue(visit.getVisitDate().isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    void testMultipleVisitsForSameVisitor() {
        Visitor visitor = new Visitor();
        visitor.setName("Dana");
        visitor.setPhoneNumber("2223334444");
        visitor.setEmail("dana@example.com");
        visitor.setAddress("101 Pine St");

        Visit firstVisit = visitService.registerVisit(visitor);
        Visit secondVisit = visitService.registerVisit(visitor);

        assertNotNull(firstVisit);
        assertNotNull(secondVisit);
        assertEquals(firstVisit.getVisitor().getEmail(), secondVisit.getVisitor().getEmail());

        // There should be two visits for the same visitor
        long visitCount = visitRepository.findAll().stream()
                .filter(v -> v.getVisitor().getEmail().equals("dana@example.com"))
                .count();
        assertEquals(2, visitCount);
    }
}