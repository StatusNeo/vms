package com.statusneo.vms.service;

import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class VisitService {

    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(4);

    @Autowired
    private OtpService otpService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VisitRepository visitRepository;

    private static final Logger logger = LoggerFactory.getLogger(VisitService.class);



    //    @Transactional(readOnly = true)
    public boolean visitorExists(String email) {
        return visitorRepository.existsByEmail(email);
    }

    // Register visit with OTP
    public Visit registerVisit(Visit visit) {
        visit.setOtp(otpService.generatedOtp());
        visit.setVisitDate(LocalDateTime.now());

        // Save visitor details
        return visitRepository.save(visit);

        // Send OTP
//        notificationService.sendOtp(visitor.getEmail(), visitor.getOtp());
    }
//
//    @Transactional
//    public Visitor saveVisitor(Visitor visitor) {
//        Visitor savedVisitor = visitorRepository.save(visitor);
//        emailService.sendVisitorEmail(savedVisitor);
//        otpService.sendOtp(savedVisitor.getEmail());
//        return savedVisitor;
//    }


//    public Visitor registerVisitor(Visitor visitor) {
//        // Save visitor
//        Visitor savedVisitor = visitorRepository.save(visitor);
//
//        try {
//            // Send Excel report to admin automatically
//            emailService.sendVisitorData("arshu.rashid.khan@gmail.com");  // Change to the recipient email
//        } catch (MessagingException | IOException e) {
//            e.printStackTrace(); // Log the error
//        }
//
//        return savedVisitor;
//    }


    public CompletableFuture<Visitor> saveVisitorAsync(Visitor visitor) {
        // Validate before save
        if (visitorExists(visitor.getEmail())) {
            throw new IllegalStateException("Visitor already exists");
        }

        return CompletableFuture.supplyAsync(() -> {
            Visitor savedVisitor = visitorRepository.save(visitor);
            asyncSendNotifications(savedVisitor);
            return savedVisitor;
        }, asyncExecutor);
    }

    private void asyncSendNotifications(Visitor visitor) {
        asyncExecutor.execute(() -> {
            try {
                emailService.sendVisitorEmail(visitor);
                otpService.sendOtp(visitor.getEmail());
            } catch (Exception e) {
                logger.error("Async notification failed", e);
            }
        });
    }

    @Transactional
    public Visitor saveVisitor(Visitor visitor) {
        // Simply save the visitor without checking for duplicates
        Visitor savedVisitor = visitorRepository.save(visitor);

        // Send notifications asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendVisitorEmail(savedVisitor);
                otpService.sendOtp(savedVisitor.getEmail());
            } catch (Exception e) {
                logger.error("Async notification failed", e);
            }
        });

        return savedVisitor;
    }

    public List<Visitor> getAllVisitors() {
        return visitorRepository.findAll();
    }


}
