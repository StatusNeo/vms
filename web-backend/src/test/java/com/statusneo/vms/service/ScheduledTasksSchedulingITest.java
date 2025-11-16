package com.statusneo.vms.service;

import com.statusneo.vms.config.TestConfig;
import com.statusneo.vms.dto.DirectorySyncResult;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"vms.scheduled.report.rate=500","vms.scheduled.report.initialDelay=PT0S"}) // 0.5 seconds
@EnableScheduling
@Import(TestConfig.class)
@ActiveProfiles("test")
class ScheduledTasksSchedulingITest {

    @MockitoSpyBean
    private ScheduledTasks scheduledTasks;

    @MockitoBean
    private GraphDirectoryService graphDirectoryService;


    @BeforeEach
    void setUp() {
        clearInvocations(scheduledTasks);
        DirectorySyncResult mockResult = new DirectorySyncResult(0, "dummy-delta-link");
        when(graphDirectoryService.syncAllUsersToEmployees()).thenReturn(mockResult);
    }

    @Test
    void testScheduledTaskIsInvoked() {
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> verify(scheduledTasks, atLeastOnce()).sendVisitorReport());
    }
}
