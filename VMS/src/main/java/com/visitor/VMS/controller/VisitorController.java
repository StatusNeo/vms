package com.visitor.VMS.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.visitor.VMS.model.Visitor;
import com.visitor.VMS.repository.VisitorRepository;
import com.visitor.VMS.service.NotificationService;
import com.visitor.VMS.service.OtpService;

@RestController
@RequestMapping("/api/visitor")
public class VisitorController {
	
	@Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/register")
    public ResponseEntity<?> registerVisitor(@RequestBody Visitor visitor) {
    	System.out.println("======================= Inside register api ===================");
        visitor.setOtp(otpService.generateOtp());
        visitor.setVisitDate(LocalDateTime.now());
        
        // Save visitor details
        visitorRepository.save(visitor);
        
        // Send OTP
        notificationService.sendOtp(visitor.getEmail(), visitor.getOtp());

        return ResponseEntity.ok("OTP sent to email");
    }
    
//    @GetMapping("/demoRegister")
//    public String dummyRegisterVisitor() {
//    	return "demo";
//    }
    
    @PostMapping("/verifyOtp")
    public ResponseEntity<?> verifyOtp(@RequestParam Long visitorId, @RequestParam String otp) {
    	System.out.println("================= Inside verify otp ====================");
        Visitor visitor = visitorRepository.findById(visitorId).orElseThrow(() -> new RuntimeException("Visitor not found"));
        
        if (otpService.verifyOtp(otp, visitor.getOtp())) {
            visitor.setIsApproved(true);
            visitorRepository.save(visitor);
            notificationService.sendMeetingNotification(visitor.getWhomToMeet(), visitor.getName());
            return ResponseEntity.ok("OTP verified and visitor approved");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
    }

    @GetMapping("/report")
    public ResponseEntity<?> getReport(@RequestParam String period) {
        List<Visitor> visitors;
        if (period.equals("daily")) {
            visitors = visitorRepository.findAllByVisitDateBetween(
                LocalDateTime.now().toLocalDate().atStartOfDay(), LocalDateTime.now()
            );
        } else if (period.equals("monthly")) {
            visitors = visitorRepository.findAllByVisitDateBetween(
                LocalDateTime.now().minusMonths(1), LocalDateTime.now()
            );
        } else {
            return ResponseEntity.badRequest().body("Invalid period");
        }
        return ResponseEntity.ok(visitors);
    }
}
