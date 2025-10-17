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

import com.statusneo.vms.dto.VerificationResult;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class VisitService {

    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(4);

    private final OtpService otpService;

    private final VisitorRepository visitorRepository;

    private final NotificationService notificationService;

    private final EmailService emailService;

    private final VisitRepository visitRepository;

    private static final Logger logger = LoggerFactory.getLogger(VisitService.class);

    public VisitService(OtpService otpService, VisitorRepository visitorRepository, NotificationService notificationService, EmailService emailService, VisitRepository visitRepository) {
        this.otpService = otpService;
        this.visitorRepository = visitorRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.visitRepository = visitRepository;
    }


    // Send OTP
//        notificationService.sendOtp(visitor.getEmail(), visitor.getOtp());
//    }
//
//    @Transactional
//    public Visitor saveVisitor(Visitor visitor) {
//        Visitor savedVisitor = visitorRepository.save(visitor);
//        emailService.sendVisitorEmail(savedVisitor);
//        otpService.sendOtp(savedVisitor.getEmail());
//        return savedVisitor;
//    }


//        return otpService.sendOtp(visit.getVisitor().getEmail(), visit);
//    }

    /**
     * Registers a new visit and generates an OTP for visitor verification.
     * The OTP is associated with the visit and sent to the visitor's email.
     *
     * @param visitor The visitor to register
     * @return The saved visit entity
     */
    @Transactional
    public Visit registerVisit(Visitor visitor) {
        Visitor savedVisitor = visitorRepository.save(visitor);

        Visit visit = new Visit();
        visit.setVisitor(savedVisitor);
        // Populate host (string) from the saved Visitor's host (Employee) if present
        if (savedVisitor.getHost() != null) {
            visit.setHost(savedVisitor.getHost().getName());
        }
        visit.setVisitDate(LocalDateTime.now());

        Visit savedVisit = visitRepository.save(visit);

        CompletableFuture.runAsync(() -> {
            try {
                otpService.generateOtp(savedVisit); // Updated call
            } catch (Exception e) {
                logger.error("Async OTP generation failed", e);
            }
        }, asyncExecutor);

        return savedVisit;
    }

    @Transactional
    public VerificationResult confirmVisit(Long visitId, String otpCode) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found for ID: " + visitId));

        VerificationResult result = otpService.validateOtp(visit, otpCode);

        if (result.success()) {
            visit.setIsApproved(true);
            visitRepository.save(visit);

            Visitor visitor = visit.getVisitor();
            Employee host = (visitor != null) ? visitor.getHost() : null;

            CompletableFuture.runAsync(() -> {
                try {
                    if (visitor != null) {
                        notificationService.sendVisitorConfirmationEmail(visitor);
                    }
                    if (host != null) {
                        notificationService.sendHostNotification(visitor, host);
                    }
                } catch (Exception e) {
                    // Log system error but do not propagate to the caller/UI
                    logger.error("Failed to send notifications for visitId: {}", visitId, e);
                }
            }, asyncExecutor);
        }
        return result;
    }
}
