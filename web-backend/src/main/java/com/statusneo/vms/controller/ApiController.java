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
package com.statusneo.vms.controller;

import com.statusneo.vms.cache.EmployeeNameCache;
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.repository.VisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private EmployeeNameCache employeeNameCache;

    @GetMapping("/report")
    public ResponseEntity<?> getReport(@RequestParam String period) {
        logger.info("API: Generating report for period: {}", period);

        List<Visit> visits;
        if (period.equals("daily")) {
            visits = visitRepository.findAllByVisitDateBetween(
                    LocalDateTime.now().toLocalDate().atStartOfDay(),
                    LocalDateTime.now()
            );
        } else if (period.equals("monthly")) {
            visits = visitRepository.findAllByVisitDateBetween(
                    LocalDateTime.now().minusMonths(1),
                    LocalDateTime.now()
            );
        } else {
            return ResponseEntity.badRequest().body("Invalid period. Use 'daily' or 'monthly'.");
        }

        return ResponseEntity.ok(visits);
    }

    @GetMapping("/refresh-employee-cache")
    public ResponseEntity<String> refreshEmployeeCache() {
        logger.info("API: Refreshing employee cache");
        employeeNameCache.initializeCache();
        return ResponseEntity.ok("Employee cache refreshed successfully");
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("API is healthy");
    }
}