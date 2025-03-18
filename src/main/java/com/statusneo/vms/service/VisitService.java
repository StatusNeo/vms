package com.statusneo.vms.service;

import com.statusneo.vms.model.VisitingInfo;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitingInfoRepository;
import com.statusneo.vms.repository.VisitorRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitService {

    @Autowired
    private OtpService otpService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VisitingInfoRepository visitingInfoRepository;

    public VisitingInfo registerVisit(VisitingInfo visitingInfo) {
        visitingInfo.setOtp(otpService.generatedOtp());
        visitingInfo.setVisitDate(LocalDateTime.now());

        // Save visitor details
        VisitingInfo id = visitingInfoRepository.save(visitingInfo);
        return id;

        // Send OTP
//        notificationService.sendOtp(visitor.getEmail(), visitor.getOtp());
    }

    @Transactional
    public Visitor saveVisitor(Visitor visitor) {
        Visitor savedVisitor = visitorRepository.save(visitor);
        emailService.sendVisitorEmail(savedVisitor);
        otpService.sendOtp(savedVisitor.getEmail());
        return savedVisitor;
    }


    public Visitor registerVisitor(Visitor visitor) {
        // Save visitor
        Visitor savedVisitor = visitorRepository.save(visitor);

        try {
            // Send Excel report to admin automatically
            emailService.sendVisitorData("arshu.rashid.khan@gmail.com");  // Change to the recipient email
        } catch (MessagingException | IOException e) {
            e.printStackTrace(); // Log the error
        }

        return savedVisitor;
    }

    public List<Visitor> getAllVisitors() {
        return visitorRepository.findAll();
    }
}
