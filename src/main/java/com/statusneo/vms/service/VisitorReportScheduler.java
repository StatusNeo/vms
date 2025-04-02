//package com.statusneo.vms.service;
//
//import com.statusneo.vms.model.Visitor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.io.ByteArrayInputStream;
//import java.util.List;
//
//@Component
//public class VisitorReportScheduler {
//
//    @Autowired
//    private VisitService visitService;
//    @Autowired
//    private ExcelService excelService;
//    @Autowired
//    private EmailService emailService;
//
//    private static final Logger logger = LoggerFactory.getLogger(VisitorReportScheduler.class);
//
//
//    public VisitorReportScheduler(VisitService visitService, ExcelService excelService, EmailService emailService) {
//        this.visitService = visitService;
//        this.excelService = excelService;
//        this.emailService = emailService;
//    }
//
//    @Scheduled(fixedRate = 12 * 60 * 60 * 1000) // 12 hours in milliseconds
//
//    public void sendVisitorReport() {
//        logger.info("Starting scheduled visitor report task");
//        try {
//            List<Visitor> visitors = visitService.getAllVisitors();
//            logger.info("Fetched {} visitors from the database", visitors.size());
//            if (visitors.isEmpty()) {
//                logger.warn("No visitors found to generate report");
//                return;
//            }
//
//            byte[] excelFile = excelService.generateVisitorExcel(visitors);
//            emailService.sendEmailWithAttachment(
//                    "arshu.rashid.khan@gmail.com",
//                    "Visitor Report - " + java.time.LocalDateTime.now(),
//                    "Attached is the visitor report.",
//                    excelFile
//            );
//            logger.info("Scheduled visitor report sent successfully");
//        } catch (Exception e) {
//            logger.error("Failed to send scheduled visitor report", e);
//        }
//    }
//}