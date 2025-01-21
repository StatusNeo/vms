package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VisitService {

    @Autowired
    private OtpService otpService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VisitorRepository visitorRepository;

    public Visitor registerVisit(Visitor visitor) {
        visitor.setOtp(otpService.generateOtp());
        visitor.setVisitDate(LocalDateTime.now());

        // Save visitor details
        Visitor id = visitorRepository.save(visitor);
        return id;

        // Send OTP
//        notificationService.sendOtp(visitor.getEmail(), visitor.getOtp());
    }
}
