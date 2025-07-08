package com.statusneo.vms.service;

import com.statusneo.vms.TestcontainersConfiguration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@SpringBootTest(properties = "vms.scheduled.report.rate=500") // 0.5 seconds
@EnableScheduling
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ScheduledTasksSchedulingITest {

    @MockitoSpyBean
    private ScheduledTasks scheduledTasks;

    @BeforeEach
    void setUp() {
        clearInvocations(scheduledTasks);
    }

    @Test
    void testScheduledTaskIsInvoked() {
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> verify(scheduledTasks, atLeastOnce()).sendVisitorReport());
    }
}
