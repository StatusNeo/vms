package com.statusneo.vms.model;

public class SyncResult {
    private final boolean success;
    private final int recordsProcessed;
    private final String message;

    public SyncResult(boolean success, int recordsProcessed, String message) {
        this.success = success;
        this.recordsProcessed = recordsProcessed;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getRecordsProcessed() {
        return recordsProcessed;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "SyncResult{" +
                "success=" + success +
                ", recordsProcessed=" + recordsProcessed +
                ", message='" + message + '\'' +
                '}';
    }
}
