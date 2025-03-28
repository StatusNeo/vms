package com.statusneo.vms.service;

import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitService {


    private static final Logger logger = LoggerFactory.getLogger(VisitService.class);

    @Autowired
    private OtpService otpService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private EmailService emailService;
    @Autowired
    private ExcelService excelService;

    @Autowired
    private VisitRepository visitRepository;

    // Register visit with OTP
    public Visit registerVisit(Visit visit) {
        visit.setOtp(otpService.generatedOtp());
        visit.setVisitDate(LocalDateTime.now());

        // Save visitor details
        Visit id = visitRepository.save(visit);
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



    public List<Visitor> getAllVisitors() {
        logger.info("Fetching all visitors from the database");
        List<Visitor> visitors = visitorRepository.findAll();
        logger.info("Fetched {} visitors", visitors.size());
        return visitors;
    }

    public Visitor saveVisitorWithPicture(Visitor visitor, String picturePath) {
        visitor.setPicturePath(picturePath);
        return visitorRepository.save(visitor);
    }

}
