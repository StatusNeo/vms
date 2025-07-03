package com.statusneo.vms.service;

public interface EmailService {
    /**
     * Sends a simple email with the default sender.
     *
     * @param fromEmail Sender email address
     * @param toEmail   Recipient email address
     * @param subject   Email subject
     * @param body      Email body content
     * @return true if the email was sent successfully, false otherwise
     */
    boolean sendEmail(String fromEmail, String toEmail, String subject, String body);

}
