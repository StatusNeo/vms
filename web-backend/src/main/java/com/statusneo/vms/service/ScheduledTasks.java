package com.statusneo.vms.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final ExcelService excelService;

    public ScheduledTasks(ExcelService excelService) {
        this.excelService = excelService;
    }

    @Scheduled(fixedRate = 12 * 60 * 60 * 1000) // Runs every 12 hours
    public void sendVisitorReport() {
        excelService.sendVisitorReport();
    }
}
