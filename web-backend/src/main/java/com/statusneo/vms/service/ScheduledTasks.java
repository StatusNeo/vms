/* ********************************************************************
 *
 *  Copyright (c) 2025 Statusneo.
 *
 * All rights Reserved.
 *
 * Redistribution and use of any source or binary or in any form, without
 * written approval and permission is prohibited.
 *
 * Please read the Terms of use, Disclaimer and Privacy Policy on https://www.statusneo.com/
 *
 * **********************************************************************
 */
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
