package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitorRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final VisitorRepository visitorRepository;
    private final ExcelGeneratorService excelGeneratorService;

    @Value("${visitor.system.notification.email}")
    private String recipientEmail;

    @Value("${visitor.system.notification.subject}")
    private String notificationSubject;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${visitor.system.employee.notification.subject}")
    private String employeeNotificationSubject;

    @Autowired
    public EmailService(JavaMailSender mailSender,
                        VisitorRepository visitorRepository,
                        ExcelGeneratorService excelGeneratorService) {
        this.mailSender = mailSender;
        this.visitorRepository = visitorRepository;
        this.excelGeneratorService = excelGeneratorService;
    }

    public void sendVisitorEmail(Visitor visitor) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(recipientEmail);
            message.setSubject(notificationSubject + ": " + visitor.getName());
            message.setText(createVisitorDetailsMessage(visitor));
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send visitor email", e);
        }
    }

    private String createVisitorDetailsMessage(Visitor visitor) {
        return String.format("""
            Visitor Details:
            Name: %s
            Phone: %s
            Email: %s
            Address: %s
            """,
                visitor.getName(),
                visitor.getPhoneNumber(),
                visitor.getEmail(),
                visitor.getAddress());
    }

    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendVisitorReport() {
        try {
            List<Visitor> visitors = visitorRepository.findAll();
            byte[] excelFile = excelGeneratorService.generateExcel(visitors);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setSubject(notificationSubject + " Report");
            helper.setText("Please find attached the visitor report.");
            helper.addAttachment("Visitor_Report.xlsx", new ByteArrayResource(excelFile));

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send visitor report", e);
        }
    }
}
