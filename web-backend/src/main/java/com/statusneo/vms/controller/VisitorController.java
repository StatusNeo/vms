// src/main/java/com/statusneo/vms/controller/VisitorController.java
package com.statusneo.vms.controller;

import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.EmployeeRepository;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import com.statusneo.vms.repository.EmailService; // Ensure this imports the INTERFACE
import com.statusneo.vms.service.EmployeeService;
import com.statusneo.vms.service.ExcelService;
import com.statusneo.vms.service.FileStorageService;
import com.statusneo.vms.service.OtpService;
import com.statusneo.vms.service.PendingRegistrationService;
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

// import java.io.IOException; // REMOVE THIS IMPORT if only used for MessagingException
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
    private EmailService emailService; // Ensure this is autowiring the INTERFACE

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PendingRegistrationService pendingRegistrationService;

    // This map seems to be a redundant way to store pending visitors if PendingRegistrationService is used
    // private final Map<String, Visitor> pendingVisitors = new HashMap<>(); // Consider removing if not actively used

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
        return "index"; // Looks for src/main/resources/templates/index.html
    }

    @GetMapping("/all")
    public List<Visitor> getAllVisitors() {
        return visitService.getAllVisitors();
    }

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

    // @PostMapping("/send-report") // REMOVED as per the previous update to EmailService, you would call emailService.sendVisitorReport()
    // public String sendReport(@RequestParam String email) {
    //     try {
    //         // emailService.sendVisitorData(email); // Assuming this was refactored or removed
    //         emailService.sendVisitorReport(); // Use the dedicated method on EmailService
    //         return "Visitor report sent successfully.";
    //     } catch (Exception e) { // Catch generic Exception, as MessagingException/IOException might not be thrown by Graph API calls
    //         logger.error("Error sending report: {}", e.getMessage());
    //         return "Error sending email: " + e.getMessage();
    //     }
    // }

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

    @PostMapping("/submit-details")
    public ResponseEntity<String> submitDetails(@RequestBody Visitor visitor) {
        // Store details temporarily
        pendingRegistrationService.savePendingVisitor(visitor.getEmail(), visitor);

        // Send OTP
        otpService.sendOtp(visitor.getEmail());
        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtpForRegistration(@RequestBody Visitor visitor) {
        // 1. Validate inputs (consider adding more robust validation here)
        if (visitor.getEmail() == null || visitor.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required for OTP.");
        }
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

        // Store visitor details temporarily
        pendingRegistrationService.savePendingVisitor(visitor.getEmail(), visitor);

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

        // Get pending visitor details
        Visitor visitor = pendingRegistrationService.getAndRemove(email);
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
            logger.error("Error during complete registration for email {}: {}", email, e.getMessage());
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
            logger.error("Error sending OTP for email verification to {}: {}", email, e.getMessage());
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
            logger.error("Error during visitor registration for email {}: {}", visitor.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registering visitor: " + e.getMessage());
        }
    }
}