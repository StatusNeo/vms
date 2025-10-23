package com.statusneo.vms.service;

import com.statusneo.vms.model.SyncAudit;
import com.statusneo.vms.repository.SyncAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile({"prod", "default"})
public class StartupTasks {

    private static final Logger log = LoggerFactory.getLogger(StartupTasks.class);

    private final GraphDirectoryService graphDirectoryService;
    private final TaskExecutor taskExecutor;
    private final SyncAuditRepository syncAuditRepository;

    // config property to enable/disable startup sync
    private final boolean startupSyncEnabled;

    private static final String SYNC_TYPE_FULL_USERS = "FULL_USER_SYNC";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    public StartupTasks(GraphDirectoryService graphDirectoryService,
                        @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor,
                        SyncAuditRepository syncAuditRepository,
                        @Value("${vms.sync.onstartup.enabled:true}") boolean startupSyncEnabled) {
        this.graphDirectoryService = graphDirectoryService;
        this.taskExecutor = taskExecutor;
        this.syncAuditRepository = syncAuditRepository;
        this.startupSyncEnabled = startupSyncEnabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncEmployeesFromGraphOnStartup() {
        if (!startupSyncEnabled) {
            log.info("Startup Graph sync is disabled by property vms.sync.onstartup.enabled=false");
            return;
        }

        // If a successful full sync already exists, skip the heavy full sync.
        if (syncAuditRepository.existsBySyncTypeAndStatus(SYNC_TYPE_FULL_USERS, STATUS_SUCCESS)) {
            log.info("A previous successful full user sync was detected; skipping full startup sync.");
            return;
        }

        log.info("Scheduling employees sync from Graph on startup (async)");

        taskExecutor.execute(() -> {
            SyncAudit audit = new SyncAudit();
            audit.setSyncType(SYNC_TYPE_FULL_USERS);
            audit.setStatus(STATUS_IN_PROGRESS);
            audit.setStartTime(LocalDateTime.now());
            audit = syncAuditRepository.save(audit);

            int upserts = 0; // track progress and ensure available in catch
            try {
                upserts = graphDirectoryService.syncAllUsersToEmployees();

                audit.setEndTime(LocalDateTime.now());
                audit.setRecordsProcessed(upserts);
                audit.setStatus(STATUS_SUCCESS);
                audit.setErrorMessage(null);
                log.info("Successfully synced {} employees from Graph", upserts);

                syncAuditRepository.save(audit);
            } catch (Exception e) {
                audit.setEndTime(LocalDateTime.now());
                audit.setRecordsProcessed(upserts);
                audit.setStatus(STATUS_FAILED);
                audit.setErrorMessage(e.getMessage());
                syncAuditRepository.save(audit);
                log.error("Failed to sync users from Graph to employees during startup", e);
            }
        });
    }
}
