package com.statusneo.vms.controller;

import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.EmployeeRepository;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import com.statusneo.vms.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private VisitService visitService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmployeeService employeeService;


    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ExcelService excelService;

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
//
//    @PostMapping("/send-report")
//    public String sendReport(@RequestParam String email) {
//        try {
//            emailService.sendVisitorData(email);  // ✅ Method is called here
//            return "Visitor report sent successfully to " + email;
//        } catch (MessagingException | IOException e) {
//            return "Error sending email: " + e.getMessage();
//        }
//    }

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
//            Visitor savedVisitor = visitService.registerVisit(visitor);
            return ResponseEntity.ok("<p class='text-green-600 font-bold'>Visitor registered successfully!</p>");
        } catch (IllegalStateException e) {
            // Custom message when visitor exists
            return ResponseEntity.ok("<p class='text-blue-600 font-bold'>Welcome back! Your visit has been recorded.</p>");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("<p class='text-red-600'>Error: " + e.getMessage() + "</p>");
        }
    }

//    @PostMapping("/verify-email")
//    public ResponseEntity<String> verifyEmail(@RequestParam String email) {
//        if (visitorRepository.existsByEmail(email)) {
//            return ResponseEntity.badRequest().body("Email already registered");
//        }
//        otpService.sendOtp(email);
//        return ResponseEntity.ok("OTP sent successfully");
//    }

//
//    @PostMapping("/register")
//    public ResponseEntity<String> registerVisitor(@RequestBody Visitor visitor) {
//        if (!otpService.isEmailVerified(visitor.getEmail())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body("Email not verified");
//        }
//        Visitor savedVisitor = visitorRepository.save(visitor);
//        otpService.clearVerifiedEmail(visitor.getEmail());
//        return ResponseEntity.ok("Registration successful");
//    }


    @PostMapping("/submit-details")
    public ResponseEntity<String> submitDetails(@RequestBody Visitor visitor) {
        // Store details temporarily

        // Send OTP
        otpService.sendOtp(visitor.getEmail());
        return ResponseEntity.ok("OTP sent to email");
    }

//    // Updated registration endpoint
//    @PostMapping("/complete-registration")
//    public ResponseEntity<String> completeRegistration(
//            @RequestParam String email,
//            @RequestParam String otp) {
//
//        // 1. Validate OTP
//        if (!otpService.validateOtp(email, otp)) {
//            return ResponseEntity.badRequest().body("Invalid OTP");
//        }
//
//        // 2. Get saved details
//        Visitor visitor = pendingRegistrationService.getAndRemove(email);
//        if (visitor == null) {
//            return ResponseEntity.badRequest().body("Session expired. Please restart registration.");
//        }
//
//        // 3. Save to DB
//        Visitor savedVisitor = visitorRepository.save(visitor);
//        return ResponseEntity.ok("<p class='text-green-600 font-bold'>Registration successful!</p>");
//    }

    // New endpoint in your Controller
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtpForRegistration(@RequestBody Visitor visitor) {
        // 1. Validate inputs
        // 2. Send OTP
        otpService.sendOtp(visitor.getEmail());
        return ResponseEntity.ok("OTP sent");
    }

    @PostMapping("/initiate-registration")
    public ResponseEntity<String> initiateRegistration(@RequestBody Visitor visitor) {
        // Validate required fields
        if (visitor.getEmail() == null || visitor.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (visitor.getName() == null || visitor.getName().isEmpty()) {
            return ResponseEntity.badRequest().body("Name is required");
        }

        // Check if email already exists
        if (visitorRepository.existsByEmail(visitor.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        // Generate and send OTP
        otpService.sendOtp(visitor.getEmail());

        return ResponseEntity.ok("OTP sent to your email");
    }

    @PostMapping("/complete-registration")
    public ResponseEntity<String> completeRegistration(
            @RequestParam String email,
            @RequestParam String otp) {

        // Validate OTP
        if (!otpService.validateOtp(email, otp)) {
            return ResponseEntity.badRequest().body("""
                    <div id="otp-error-message" class="text-red-600 font-bold mb-4">
                        Invalid OTP, please try again
                    </div>
                    """);
        }

        Visitor visitor = null;
        // Get pending visitor details
        if (visitor == null) {
            return ResponseEntity.badRequest().body("Registration session expired. Please start again.");
        }

        try {
            // Save visitor to database
            Visitor savedVisitor = visitorRepository.save(visitor);

            // Send notification to admin
            emailService.sendVisitorEmail(savedVisitor);

            return ResponseEntity.ok("""
                    <p class="text-green-600 font-bold">Registration successful!</p>
                    """);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("""
                    <p class="text-red-600">Error during registration: """ + e.getMessage() + "</p>");
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String email) {
        try {
            // 1. Validate email format only
            if (email == null || !email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return ResponseEntity.badRequest().body("""
                        <div class="text-red-600 mb-2">
                            Please enter a valid email address
                        </div>
                        """);
            }

            // 2. Send OTP (no email uniqueness check)
            otpService.sendOtp(email);

            return ResponseEntity.ok("""
                    <div class="text-green-600 mb-2">
                        OTP sent successfully! Please check your email.
                    </div>
                    """);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("""
                            <div class="text-red-600 mb-2">
                                Error sending OTP. Please try again.
                            </div>
                            """);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerVisitor(@RequestBody Visitor visitor) {
        try {
            // Validate required fields
            if (visitor.getEmail() == null || visitor.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }

            // Save visitor to database
            Visitor savedVisitor = visitorRepository.save(visitor);

            // Send notification email
            emailService.sendVisitorEmail(savedVisitor);

            // Send OTP to visitor
            otpService.sendOtp(visitor.getEmail());

            return ResponseEntity.ok("Visitor registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registering visitor: " + e.getMessage());
        }
    }


}