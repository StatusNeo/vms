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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    private  FileStorageService fileStorageService;

    private final Map<String, Visitor> pendingVisitors = new HashMap<>();

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

//    @PostMapping("/api/visitors/saveVisitor")
//    public Map<String, String> saveVisitor(@RequestBody Visitor visitor) {
//        logger.info("Received request to save visitor: {}", visitor);
//        Map<String, String> response = new HashMap<>();
//        //visitService.saveVisitor(visitor);
//
//        try {
//            pendingVisitors.put(visitor.getEmail(), visitor);
//            otpService.sendOtp(visitor.getEmail());
//            response.put("status", "success");
//            response.put("message", "An OTP has been sent to your email.");
//            response.put("email", visitor.getEmail());
//            return response;
//        } catch (Exception e) {
//            logger.error("Failed to process visitor registration for email: {}", visitor.getEmail(), e);
//            response.put("status", "error");
//            response.put("message", "Failed to send OTP: " + e.getMessage());
//            return response;
//        }
//    }

    @GetMapping("/all")
    public List<Visitor> getAllVisitors() {
        return visitService.getAllVisitors();
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<String> validateOtp(@RequestParam String email, @RequestParam String otp) {
        boolean isValid = otpService.validateOtp(email, otp);
        return isValid ? ResponseEntity.ok("<p class='text-green-600 font-bold'>OTP Verified Successfully!</p>") :
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

//    @PostMapping("/excelSend")
//    public Visitor registerVisitor(@RequestBody Visitor visitor) {
//        return visitService.registerVisitor(visitor);
//    }

//    @GetMapping("/send-visitor-report")
//    public String sendVisitorReport() {
//        logger.info("Received request to send visitor report manually");
//        try {
//            List<Visitor> visitors = visitService.getAllVisitors();
//            logger.info("Fetched {} visitors from the database", visitors.size());
//            if (visitors.isEmpty()) {
//                logger.warn("No visitors found to generate report");
//                return "No visitors found to generate report";
//            }
//
//            byte[] excelFile = excelService.generateVisitorExcel(visitors);
//            emailService.sendVisitorReport(
//                    "arshu.rashid.khan@gmail.com",
//                    "Manual Visitor Report - " + java.time.LocalDateTime.now(),
//                    "Attached is the manually requested visitor report.",
//                    excelFile
//            );
//            logger.info("Manual visitor report sent successfully");
//            return "Visitor report sent successfully!";
//        } catch (Exception e) {
//            logger.error("Failed to send manual visitor report", e);
//            return "Failed to send visitor report: " + e.getMessage();
//        }
//    }
//

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

//    @PostMapping("/uploadPicture")
//    public Map<String, Object> uploadPicture(@RequestParam String email, @RequestParam("picture") MultipartFile picture) {
//        logger.info("Received request to upload picture for email: {}", email);
//        Map<String, Object> response = new HashMap<>();
//        try {
//            Visitor visitor = pendingVisitors.get(email);
//            if (visitor == null) {
//                logger.warn("No pending visitor found for email: {}", email);
//                response.put("status", "error");
//                response.put("message", "Error: Visitor data not found. Please try again.");
//                return response;
//            }
//
//            // Save the picture to the filesystem
//            String picturePath = fileStorageService.storeFile(picture);
//            logger.info("Picture saved at: {}", picturePath);
//
//            // Save the visitor with the picture path
//            Visitor savedVisitor = visitService.saveVisitorWithPicture(visitor, picturePath);
//            logger.info("Visitor saved successfully with picture: {}", savedVisitor);
//
//            // Remove the visitor from pendingVisitors
//            pendingVisitors.remove(email);
//
//            // Return the visitor details and picture path
//            response.put("status", "success");
//            response.put("message", "Visitor registered successfully!");
//            response.put("visitor", savedVisitor);
//            response.put("pictureUrl", "/" + picturePath); // URL to access the image
//            return response;
//        } catch (Exception e) {
//            logger.error("Failed to upload picture for email: {}", email, e);
//            response.put("status", "error");
//            response.put("message", "Failed to upload picture: " + e.getMessage());
//            return response;
//        }
//    }
//
//


}
