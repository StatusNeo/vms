package com.statusneo.vms.repository;

import com.statusneo.vms.model.SyncAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncAuditRepository extends JpaRepository<SyncAudit, Long> {
    boolean existsBySyncTypeAndStatus(String syncType, String status);
}
