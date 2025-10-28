-- Flyway migration to create sync_audit table
-- Version: 2

CREATE TABLE IF NOT EXISTS sync_audit (
    id BIGSERIAL PRIMARY KEY,
    sync_type VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time TIMESTAMP WITHOUT TIME ZONE,
    records_processed INTEGER,
    error_message VARCHAR(2000)
);

-- Optional index to speed up lookup by type+status
CREATE INDEX IF NOT EXISTS idx_sync_audit_type_status ON sync_audit (sync_type, status);

