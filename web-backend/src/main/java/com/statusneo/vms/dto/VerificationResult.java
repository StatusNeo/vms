package com.statusneo.vms.dto;

public class VerificationResult {
    private boolean success;
    private boolean reattempt;
    private String message;

    public VerificationResult(boolean success, boolean reattempt, String message) {
        this.success = success;
        this.reattempt = reattempt;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }


    public boolean isReattempt() {
        return reattempt;
    }

    public String getMessage() {
        return message;
    }
}

