package com.statusneo.vms.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final EmailService emailService;

    public ScheduledTasks(EmailService emailService) {
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 12 * 60 * 60 * 1000) // Runs every 12 hours
    public void sendVisitorReport() {
        String recipientEmail = "arshu.rashid.khan@gmail.com"; // Change as needed
        emailService.sendVisitorReport(recipientEmail);
    }
}
