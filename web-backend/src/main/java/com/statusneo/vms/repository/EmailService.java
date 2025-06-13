package com.statusneo.vms.repository;

import java.util.List;

import com.statusneo.vms.model.Visitor;

public interface EmailService {

    void sendEmail(String toEmail, String subject, String body);

    void sendVisitorEmail(Visitor visitor);

    void sendVisitorReport();

    void sendOtp(String email, String otp);

    void sendMeetingNotification(String email, String whomToMeet);

    // Optional: for QA to inspect sent emails (only relevant for the mock)
    // Production implementation can return empty list or throw UnsupportedOperationException
    List<String> getSentEmails();
    
    void clearSentEmails();
}