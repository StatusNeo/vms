/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.statusneo.vms.service;

import com.statusneo.vms.dto.DirectorySyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ScheduledTasks {

    private final ExcelService excelService;
    private final GraphDirectoryService graphDirectoryService;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);


    public ScheduledTasks(ExcelService excelService, GraphDirectoryService graphDirectoryService) {
        this.excelService = excelService;
        this.graphDirectoryService = graphDirectoryService;
    }


    @Scheduled(
            fixedRateString = "${vms.scheduled.report.rate:43200000}",
            initialDelayString = "${vms.scheduled.report.initialDelay:PT2H}"
    )


    @Scheduled(fixedRateString = "${vms.scheduled.report.rate:43200000}", initialDelayString = "PT2H") // Runs every 12 hours by default
    public void sendVisitorReport() {
        excelService.sendVisitorReport();
    }

    // @Scheduled(cron = "0 0 1 * * *")
    public void syncEmployeesFromGraph() {
        try {
            DirectorySyncResult result = graphDirectoryService.syncAllUsersToEmployees();
            log.info("Scheduled sync completed. Upserts: {}, Delta link: {}", 
                    result.upserts(), result.deltaLink() != null ? "received" : "not available");
        } catch (Exception e) {
            log.error("Failed to sync users from Graph to employees", e);
            // consider adding metrics/alerts or retrying with backoff here
        }
    }
}
