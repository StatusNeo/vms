package com.statusneo.vms.controller;

import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.EmployeeRepository;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import com.statusneo.vms.service.EmailService;
import com.statusneo.vms.service.EmployeeService;
import com.statusneo.vms.service.ExcelService;
import com.statusneo.vms.service.FileStorageService;
import com.statusneo.vms.service.NotificationService;
import com.statusneo.vms.service.OtpService;
import com.statusneo.vms.service.VisitService;
import jakarta.mail.MessagingException;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

    private static final Logger logger = LoggerFactory.getLogger(VisitorController.class);

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

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private FileStorageService fileStorageService;

    private final Map<String, Visitor> pendingVisitors = new HashMap<>();

//    @PostMapping("/register")
//    public ResponseEntity<?> registerVisitor(@RequestBody Visit visit) {
//        visitService.registerVisit(visit);
//        return ResponseEntity.ok("OTP sent to email");
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

//    @PostMapping("/saveVisitor")
//    public ResponseEntity<String> saveVisitor(@RequestBody Visitor visitor) {
//        visitService.saveVisitor(visitor);
//        return ResponseEntity.ok("<p class='text-green-600 font-bold'>Visitor registered successfully!</p>");
//    }


    @GetMapping("/all")
    public List<Visitor> getAllVisitors() {
        return visitService.getAllVisitors();
    }

//    @PostMapping("/validate-otp")
//    public ResponseEntity<String> validateOtp(@RequestParam String email, @RequestParam String otp) {
//        boolean isValid = otpService.validateOtp(email, otp);
//
//        return isValid ? ResponseEntity.ok("<p class='text-green-600 font-bold'>OTP Verified Successfully!</p>") :
//                ResponseEntity.badRequest().body("Invalid or expired OTP!");
//    }

    @PostMapping("/validate-otp") // Changed to POST for better practice
    public ResponseEntity<String> validateOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        boolean isValid = otpService.validateOtp(email, otp);

        if (isValid) {
            return ResponseEntity.ok("""
                <p class="text-green-600 font-bold">OTP Verified Successfully!</p>
            """);
        } else {
            return ResponseEntity.ok("""
            <div id="otp-error-message" class="text-red-600 font-bold mb-4">
                Invalid OTP, please try again
            </div>
            """);
        }
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

    @GetMapping("/search")
    public String searchEmployees(@RequestParam("employee") String query) {
        logger.info("Received search request for employee: {}", query);
        List<Employee> employees = employeeService.searchEmployeesByName(query);
        StringBuilder html = new StringBuilder();
        for (Employee employee : employees) {
            html.append("<li class='px-3 py-2 hover:bg-gray-100 cursor-pointer' onclick='selectEmployee(\"")
                    .append(employee.getName())
                    .append("\")'>")
                    .append(employee.getName())
                    .append("</li>");
        }
        return html.toString();
    }

    @PostMapping("/saveVisitor")
    public ResponseEntity<String> saveVisitor(@RequestBody Visitor visitor) {
        try {
            Visitor savedVisitor = visitService.saveVisitor(visitor);
            return ResponseEntity.ok("<p class='text-green-600 font-bold'>Visitor registered successfully!</p>");
        } catch (IllegalStateException e) {
            // Custom message when visitor exists
            return ResponseEntity.ok("<p class='text-blue-600 font-bold'>Welcome back! Your visit has been recorded.</p>");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("<p class='text-red-600'>Error: " + e.getMessage() + "</p>");
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String email) {
        if (visitorRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        otpService.sendOtp(email);
        return ResponseEntity.ok("OTP sent successfully");
    }


    @PostMapping("/register")
    public ResponseEntity<String> registerVisitor(@RequestBody Visitor visitor) {
        if (!otpService.isEmailVerified(visitor.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Email not verified");
        }
        Visitor savedVisitor = visitorRepository.save(visitor);
        otpService.clearVerifiedEmail(visitor.getEmail());
        return ResponseEntity.ok("Registration successful");
    }

}
