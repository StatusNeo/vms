package com.statusneo.vms.service;

import com.statusneo.vms.dto.ApiResponse;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VisitorServiceImpl {

    @Autowired
    private VisitorRepository visitorRepository;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private OtpService otpService;

    private final Map<String, Visitor> pendingVisitors = new HashMap<>();

    private static final String EMAIL_REGEX = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    // Render search results as HTML
    public String renderSearchResultsHtml(String query) {
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

    // Save Visitor (visit registration)
    private static final String VISITOR_REGISTERED = "Visitor registered successfully!";
    private static final String VISITOR_RETURNED = "Welcome back! Your visit has been recorded.";

    public ResponseEntity<String> saveVisitor(Visitor visitor) {
        try {
            // visitService.registerVisit(visitor);
            return ResponseEntity.ok(VISITOR_REGISTERED);
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(VISITOR_RETURNED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Send OTP for submit-details
    public ResponseEntity<String> submitVisitorDetails(Visitor visitor) {
        otpService.sendOtp(visitor.getEmail());
        return ResponseEntity.ok("OTP sent to email");
    }

    // send OTP for registration
    public ResponseEntity<String> sendOtpForRegistration(Visitor visitor) {
        otpService.sendOtp(visitor.getEmail());
        return ResponseEntity.ok("OTP sent");
    }

    //  Initiate registration (store visitor, send OTP)
    public ResponseEntity<String> initiateRegistration(Visitor visitor) {

        if (visitor.getEmail() == null || visitor.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (visitor.getName() == null || visitor.getName().isEmpty()) {
            return ResponseEntity.badRequest().body("Name is required");
        }
        if (visitorRepository.existsByEmail(visitor.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        pendingVisitors.put(visitor.getEmail(), visitor);
        otpService.sendOtp(visitor.getEmail());
        return ResponseEntity.ok("OTP sent to your email");
    }


    private static final String OTP_EXCEEDED = "You have exceeded the maximum number of OTP attempts. Please restart the registration process.";
    private static final String OTP_SUCCESS = "OTP Verified Successfully!";
    private static final String OTP_INVALID = "Invalid OTP, please try again";

    // OTP validation
    public ResponseEntity<ApiResponse> validateOtp(String email, String otp) {
        ApiResponse response;
        if (otpService.hasExceededOtpAttempts(email)) {
            pendingVisitors.remove(email);
            response = new ApiResponse("error", OTP_EXCEEDED);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        boolean isValid = otpService.validateOtp(email, otp);
        if (isValid) {
            otpService.markEmailAsVerified(email);
            response = new ApiResponse("success", OTP_SUCCESS);
            return ResponseEntity.ok(response);
        } else {
            if (otpService.hasExceededOtpAttempts(email)) {
                pendingVisitors.remove(email);
                response = new ApiResponse("error", OTP_EXCEEDED);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            response = new ApiResponse("error", OTP_INVALID);
            return ResponseEntity.ok(response);
        }
    }


    private static final String EMAIL_NOT_VERIFIED = "Email not verified. Please complete OTP verification.";
    private static final String SESSION_EXPIRED = "Registration session expired. Please start again.";
    private static final String REGISTRATION_SUCCESS = "Registration successful!";
    private static final String REGISTRATION_ERROR = "Error during registration: ";


    // Complete registration after OTP verified
    public ResponseEntity<ApiResponse> completeRegistration(String email) {
        if (!otpService.isEmailVerified(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("error", EMAIL_NOT_VERIFIED));
        }

        Visitor visitor = pendingVisitors.get(email);
        if (visitor == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", SESSION_EXPIRED));
        }

        try {
            visitorRepository.save(visitor);
            pendingVisitors.remove(email);
            otpService.clearVerifiedEmail(email);
            return ResponseEntity.ok(new ApiResponse("success", REGISTRATION_SUCCESS));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", REGISTRATION_ERROR + e.getMessage()));
        }
    }


    //  Resend OTP with cooldown
    private static final String INVALID_EMAIL = "Please enter a valid email address";
    private static final String OTP_RESENT_SUCCESS = "OTP resent successfully! Please check your email.";
    private static final String OTP_RESEND_WAIT = "Please wait at least 2 minutes before resending OTP.";


    public ResponseEntity<ApiResponse> resendOtp(String email) {
        if (email == null || !email.matches(EMAIL_REGEX)) {   // Use your EMAIL_REGEX constant
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", INVALID_EMAIL));
        }

        boolean resent = otpService.canResendOtp(email);
        if (resent) {
            return ResponseEntity.ok(new ApiResponse("success", OTP_RESENT_SUCCESS));
        } else {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", OTP_RESEND_WAIT));
        }
    }


    // Send OTP for just email verification
    private static final String EMAIL_REQUIRED = "Email is required";
    private static final String OTP_SENT = "OTP sent successfully! Please check your email.";
    private static final String OTP_SEND_ERROR = "Error sending OTP. Please try again.";


    public ResponseEntity<ApiResponse> emailVerification(String email) {
        try {
            if (email == null || !email.matches(EMAIL_REGEX)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse("error", INVALID_EMAIL));
            }
            otpService.sendOtp(email);
            return ResponseEntity.ok(new ApiResponse("success", OTP_SENT));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("error", OTP_SEND_ERROR));
        }
    }


    public ResponseEntity<ApiResponse> registerVisitor(Visitor visitor) {
        try {
            if (visitor.getEmail() == null || visitor.getEmail().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse("error", EMAIL_REQUIRED));
            }
            otpService.sendOtp(visitor.getEmail());
            return ResponseEntity.ok(new ApiResponse("success", VISITOR_REGISTERED));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("error", "Error registering visitor: " + e.getMessage()));
        }
    }

}
