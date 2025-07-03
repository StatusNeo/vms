package com.statusneo.vms.controller;

import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.service.OtpService;
import com.statusneo.vms.service.VisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Note: This controller is currently using deprecated OtpService methods that don't associate OTPs with Visit records.
 * In a future update, these should be replaced with the new methods that properly associate OTPs with Visit records
 * and track OTP resends. This would require retrieving or creating a Visit for each operation that sends or validates
 * an OTP.
 */

@Controller
@RequestMapping("/visit")
public class VisitController {

    private static final Logger logger = LoggerFactory.getLogger(VisitController.class);

    @Autowired
    private OtpService otpService;
    @Autowired
    private VisitService visitService;

    @RequestMapping("/error")
    public String handleError() {
        return "error";
    }

    @PostMapping("/validate-otp")
    public String validateOtp(
            @RequestParam String email,
            @RequestParam String otp,
            Model model) {
        boolean isValid = otpService.validateOtp(email, otp);
        model.addAttribute("isValid", isValid);
        model.addAttribute("message", isValid ? "OTP verified successfully!" : "Invalid OTP, please try again");
        return "fragments/otp-section";
    }

    @PostMapping("/verify-email")
    public String verifyEmail(@RequestParam String email, Model model) {
        try {
//            otpService.sendOtp(email);
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Error sending OTP. Please try again.");
            return "email-verification";
        }
        return "";
    }

    @PostMapping("/complete-registration")
    public ResponseEntity<String> completeRegistration() {

//            "Registration session expired. Please start again."
        try {
            return ResponseEntity.ok("""
                    <p class="text-green-600 font-bold">Registration successful!</p>
                    """);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("""
                    <p class="text-red-600">Error during registration: """ + e.getMessage() + "</p>");
        }
    }

    @PostMapping("/register")
    public String saveVisitor(@RequestBody Visitor visitor, Model model) {
        // Validate required fields
        if (visitor.getEmail() == null || visitor.getEmail().isEmpty()) {
//            "Email is required"
        }
        if (visitor.getEmail() == null || !visitor.getEmail().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            model.addAttribute("message", "Please enter a valid email address");
            model.addAttribute("success", false);
            return "email-verification";
        }

        if (visitor.getName() == null || visitor.getName().isEmpty()) {
//            "Name is required"
        }

        try {
            Visit visit = visitService.registerVisit(visitor);
            model.addAttribute("success", true);
            model.addAttribute("message", "Visitor registered successfully! OTP sent to your email");
            return "fragments/success-message";
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Error registering visitor: " + e.getMessage());
            return "fragments/success-message";
        }
    }
}
