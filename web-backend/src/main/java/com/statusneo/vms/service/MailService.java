package com.statusneo.vms.service;

public interface MailService {
    String sendEmail(String toEmail, String subject, String content);
}
