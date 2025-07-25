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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    @MockitoBean
    private OtpService otpService;

    @BeforeEach
    void cleanDatabase() {
        visitRepository.deleteAll();
        visitorRepository.deleteAll();
    }

    @BeforeEach
    void setupMocks() {
        doNothing().when(otpService).sendOtp(any(Visit.class));
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
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(otpService, times(1)).sendOtp(any(Visit.class)));
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

        // Verify OTP was sent for the visit
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(otpService, times(1)).sendOtp(any(Visit.class)));
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

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(otpService, times(1)).sendOtp(any(Visit.class)));
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

        long visitCount = visitRepository.findAll().stream()
                .filter(v -> v.getVisitor().getEmail().equals("dana@example.com"))
                .count();
        assertEquals(2, visitCount);

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(otpService, times(2)).sendOtp(any(Visit.class)));
    }

    @Test
    void testRegisterVisitHandlesOtpServiceFailureGracefully() {
        Visitor visitor = new Visitor();
        visitor.setName("Eve");
        visitor.setPhoneNumber("5556667777");
        visitor.setEmail("eve@example.com");
        visitor.setAddress("202 Maple St");

        doNothing().when(otpService).sendOtp(any(Visit.class));

        Visit savedVisit = visitService.registerVisit(visitor);

        // Visit should still be created successfully
        assertNotNull(savedVisit);
        assertNotNull(savedVisit.getId());
        assertEquals("Eve", savedVisit.getVisitor().getName());
        Optional<Visit> foundVisit = visitRepository.findById(savedVisit.getId());
        assertTrue(foundVisit.isPresent());

        // Verify OTP service was attempted
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(otpService, times(1)).sendOtp(any(Visit.class)));
    }

    @Test
    void testVisitEntityAssociationWithOtpService() {
        Visitor visitor = new Visitor();
        visitor.setName("Frank");
        visitor.setPhoneNumber("8889990000");
        visitor.setEmail("frank@example.com");
        visitor.setAddress("303 Birch St");

        Visit savedVisit = visitService.registerVisit(visitor);

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(otpService, times(1)).sendOtp(any(Visit.class));

                    assertNotNull(savedVisit.getVisitor());
                    assertEquals("frank@example.com", savedVisit.getVisitor().getEmail());
                    assertNotNull(savedVisit.getVisitDate());
                });
    }
}
