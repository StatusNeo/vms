package com.statusneo.vms.controller;

import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import com.statusneo.vms.service.EmailService;
import com.statusneo.vms.service.NotificationService;
import com.statusneo.vms.service.OtpService;
import com.statusneo.vms.service.VisitService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

    private static final Logger log = LoggerFactory.getLogger(VisitorController.class);
    @Autowired
    private VisitorRepository visitorRepository;
    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerVisitor(@RequestBody Visit visit) {
        visitService.registerVisit(visit);
        return ResponseEntity.ok("OTP sent to email");
    }

//    @PostMapping("/verifyOtp")
//    public ResponseEntity<?> verifyOtp(@RequestParam Long visitorId, @RequestParam String otp) {
//        Visitor visitor = visitorRepository.findById(visitorId).orElseThrow(() -> new RuntimeException("Visitor not found"));
//        VisitingInfo visitingInfo = visitingInfoRepository.findById(visitorId).orElseThrow(() -> new RuntimeException(("No Visitor")));
//
//        if (otpService.verifyOtp(otp, visitingInfo.getOtp())) {
//            visitingInfo.setIsApproved(true);
//            visitorRepository.save(visitor);
//            notificationService.sendMeetingNotification(visitingInfo.getHost(), visitor.getName());
//            return ResponseEntity.ok("OTP verified and visitor approved");
//        }
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
//    }

    @GetMapping("/report")
    public ResponseEntity<?> getReport(@RequestParam String period) {
        List<Visit> visit;
        if (period.equals("daily")) {
            visit = visitRepository.findAllByVisitDateBetween(LocalDateTime.now().toLocalDate().atStartOfDay(), LocalDateTime.now());
        } else if (period.equals("monthly")) {
            visit = visitRepository.findAllByVisitDateBetween(LocalDateTime.now().minusMonths(1), LocalDateTime.now());
        } else {
            return ResponseEntity.badRequest().body("Invalid period");
        }
        return ResponseEntity.ok(visit);
    }

    @RequestMapping("/error")
    public String handleError() {
        return "Custom error page!";
    }

    @GetMapping("/")
    public String home() {
        return "index";  // Looks for src/main/resources/templates/index.html
    }

//    @PostMapping
//    public ResponseEntity<String> saveVisitor(@RequestParam String name, @RequestParam String phoneNumber,
//                                              @RequestParam String email, @RequestParam String address) {
//        Visitor visitor = new Visitor();
//        visitor.setName(name);
//        visitor.setPhoneNumber(phoneNumber);
//        visitor.setEmail(email);
//        visitor.setAddress(address);
//
//        visitService.save(visitor);
//
//        return ResponseEntity.ok("<p class='text-green-600'>Visitor registered successfully!</p>");
//    }

    @PostMapping("/saveVisitor")
    public ResponseEntity<String> saveVisitor(@RequestBody Visitor visitor) {
        visitService.saveVisitor(visitor);
        return ResponseEntity.ok("<p class='text-green-600 font-bold'>Visitor registered successfully!</p>");
    }

    @GetMapping("/all")
    public List<Visitor> getAllVisitors() {
        return visitService.getAllVisitors();
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<String> validateOtp(@RequestParam String email, @RequestParam String otp) {
        boolean isValid = otpService.validateOtp(email, otp);
        return isValid ? ResponseEntity.ok("OTP verified successfully!") :
                ResponseEntity.badRequest().body("Invalid or expired OTP!");
    }

    @PostMapping("/send-report")
    public String sendReport(@RequestParam String email) {
        try {
            emailService.sendVisitorData(email);  // ✅ Method is called here
            return "Visitor report sent successfully to " + email;
        } catch (MessagingException | IOException e) {
            return "Error sending email: " + e.getMessage();
        }
    }

    @PostMapping("/excelSend")
    public Visitor registerVisitor(@RequestBody Visitor visitor) {
        return visitService.registerVisitor(visitor);
    }


    @PostMapping("/send")
    public ResponseEntity<String> sendReport() {
        String email = "arshu.rashid.khan@gmail.com"; // Fixed email address
        emailService.sendVisitorReport(email);
        return ResponseEntity.ok("Visitor report sent to " + email);
    }

}
