package com.statusneo.vms.repository;

public interface EmailService {

    void sendEmail(String toEmail, String subject, String body);
}