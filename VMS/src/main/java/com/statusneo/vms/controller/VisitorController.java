package com.statusneo.vms.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.statusneo.vms.service.VisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitorRepository;
import com.statusneo.vms.service.NotificationService;
import com.statusneo.vms.service.OtpService;

@RestController
@RequestMapping("/api/visitor")
public class VisitorController {

    private static final Logger log = LoggerFactory.getLogger(VisitorController.class);
    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VisitService visitService;

    @PostMapping("/register")
    public ResponseEntity<?> registerVisitor(@RequestBody Visitor visitor) {
        visitService.registerVisit(visitor);
        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<?> verifyOtp(@RequestParam Long visitorId, @RequestParam String otp) {
        Visitor visitor = visitorRepository.findById(visitorId).orElseThrow(() -> new RuntimeException("Visitor not found"));

        if (otpService.verifyOtp(otp, visitor.getOtp())) {
            visitor.setIsApproved(true);
            visitorRepository.save(visitor);
            notificationService.sendMeetingNotification(visitor.getHost(), visitor.getName());
            return ResponseEntity.ok("OTP verified and visitor approved");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
    }

    @GetMapping("/report")
    public ResponseEntity<?> getReport(@RequestParam String period) {
        List<Visitor> visitors;
        if (period.equals("daily")) {
            visitors = visitorRepository.findAllByVisitDateBetween(LocalDateTime.now().toLocalDate().atStartOfDay(), LocalDateTime.now());
        } else if (period.equals("monthly")) {
            visitors = visitorRepository.findAllByVisitDateBetween(LocalDateTime.now().minusMonths(1), LocalDateTime.now());
        } else {
            return ResponseEntity.badRequest().body("Invalid period");
        }
        return ResponseEntity.ok(visitors);
    }
}
