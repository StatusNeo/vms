package com.statusneo.vms.service;

import com.statusneo.vms.TestcontainersConfiguration;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Disabled("Enable this test with real credentials and configuration for full integration testing.")
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ExcelServiceITest {

    @Autowired
    private ExcelService excelService;

    @Autowired
    private VisitorRepository visitorRepository;

    @BeforeEach
    void setUp() {
        // Clean up and insert test visitors
        visitorRepository.deleteAll();
        Visitor v1 = new Visitor(null, "Alice Smith", "1112223333", "alice@example.com", "123 Main St");
        Visitor v2 = new Visitor(null, "Bob Jones", "4445556666", "bob@example.com", "456 Oak Ave");
        Visitor v3 = new Visitor(null, "Carol White", "7778889999", "carol@example.com", "789 Pine Rd");
        visitorRepository.saveAll(List.of(v1, v2, v3));
    }

    @Test
    @Transactional
    void testSendVisitorReport_GeneratesAndSendsExcel() {
        // This will generate the Excel and send it via EmailService (GraphEmailService in prod)
        assertDoesNotThrow(() -> excelService.sendVisitorReport(), "sendVisitorReport should not throw");
    }
}
