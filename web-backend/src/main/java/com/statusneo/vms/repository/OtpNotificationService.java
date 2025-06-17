package com.statusneo.vms.repository;

public interface OtpNotificationService {
    void sendOtp(String email, String otp);
}

