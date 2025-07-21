// =====================================================================================
// VisitorController.java
// -------------------------------------------------------------------------------------
// This controller handles all HTTP requests related to visitor management in the system.
// 
// Key responsibilities:
// - Handles visitor registration, including temporary storage of visitor details
//   during the registration and OTP verification process.
// - Provides endpoints for searching employees, saving visitors, and managing
//   the registration flow (initiate, complete, etc.).
// - Delegates business logic to service classes (e.g., OtpService, VisitorRepository).
// - Manages a pendingVisitors map to temporarily store visitor data between steps.
// 
// Note: In a more advanced version, OTP-related endpoints and logic should be moved
// to a dedicated OtpController for better separation of concerns.
// 
// Basic endpoint summary:
// - GET    /api/visitors/search           : Search employees by name
// - POST   /api/visitors/saveVisitor      : Save a visitor (register visit)
// - POST   /api/visitors/submit-details   : Send OTP to visitor's email
// - POST   /api/visitors/initiateregistration : Start registration, send OTP
// - POST   /api/visitors/otpvalidation    : Validate OTP for registration
// - POST   /api/visitors/completeregistration : Complete registration after OTP
// - POST   /api/visitors/resendotp        : Resend OTP to visitor's email
// - POST   /api/visitors/emailverification: Send OTP for email verification
// - POST   /api/visitors/register         : Register a visitor (send OTP)
// 
// The controller is annotated with @RestController and @RequestMapping("/api/visitors").
// =====================================================================================

package com.statusneo.vms.controller;

import com.statusneo.vms.dto.ApiResponse;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import com.statusneo.vms.service.EmailService;
import com.statusneo.vms.service.EmployeeService;
import com.statusneo.vms.service.OtpService;
import com.statusneo.vms.service.VisitService;
import com.statusneo.vms.service.VisitorServiceImpl;
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

import java.util.HashMap;
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
    private VisitorServiceImpl visitorService;

    // Store pending visitors by email during registration
    private final Map<String, Visitor> pendingVisitors = new HashMap<>();

    @RequestMapping("/error")
    public String handleError() {
        return "Custom error page!";
    }

    @GetMapping("/")
    public String home() {
        return "index"; 
    }

    @GetMapping("/search")
    public String searchEmployees(@RequestParam("employee") String query) {
        return visitorService.renderSearchResultsHtml(query);
    }


    @PostMapping("/savevisitor")
    public ResponseEntity<String> saveVisitor(@RequestBody Visitor visitor) {
        return visitorService.saveVisitor(visitor);
    }


    @PostMapping("/submitdetails")
    public ResponseEntity<String> submitDetails(@RequestBody Visitor visitor) {
        return visitorService.submitVisitorDetails(visitor);
    }

    @PostMapping("/sendotp")
    public ResponseEntity<String> sendOtpForRegistration(@RequestBody Visitor visitor) {
        return visitorService.sendOtpForRegistration(visitor);
    }

    @PostMapping("/initiateregistration")
    public ResponseEntity<String> initiateRegistration(@RequestBody Visitor visitor) {
        return visitorService.initiateRegistration(visitor);
    }

    @PostMapping("/otpvalidation")
    public ResponseEntity<ApiResponse> validateOtp(@RequestParam String email, @RequestParam String otp) {
        return visitorService.validateOtp(email, otp);
    }

    @PostMapping("/completeregistration")
    public ResponseEntity<ApiResponse> completeRegistration(@RequestParam String email) {
        return visitorService.completeRegistration(email);
    }

    @PostMapping("/resendotp")
    public ResponseEntity<ApiResponse> resendOtp(@RequestParam String email) {
        return visitorService.resendOtp(email);
    }

    @PostMapping("/emailverification")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String email) {
        return visitorService.emailVerification(email);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerVisitor(@RequestBody Visitor visitor) {
        return visitorService.registerVisitor(visitor);
    }
}