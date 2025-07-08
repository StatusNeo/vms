package com.statusneo.vms.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final ExcelService excelService;

    public ScheduledTasks(ExcelService excelService) {
        this.excelService = excelService;
    }

    @Scheduled(fixedRateString = "${vms.scheduled.report.rate:43200000}") // Runs every 12 hours by default
    public void sendVisitorReport() {
        excelService.sendVisitorReport();
    }
}
