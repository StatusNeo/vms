//package com.statusneo.vms.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class VisitorReportScheduler {
//
//    private final EmailService emailService;
//
//    // Runs every 12 hours (e.g., at 12 AM and 12 PM)
//    @Scheduled(cron = "0 0 0,12 * * ?")
//    public void scheduleVisitorReport() {
//        String defaultEmail = "arshu.rashid.khan@gmail.com"; // Set a valid email
//        emailService.sendVisitorReport(defaultEmail);
//    }
//}