package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitorRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VisitorRepository visitorRepository;


    @Autowired
    private ExcelGeneratorService excelGeneratorService;

    public void sendVisitorEmail(Visitor visitor) {
        String toEmail = "statusneo9@gmail.com";  // Change this to the recipient email
        String subject = "New Visitor Registered: " + visitor.getName();
        String body = "Visitor Details:\n\n" +
                "Name: " + visitor.getName() + "\n" +
                "Phone: " + visitor.getPhoneNumber() + "\n" +
                "Email: " + visitor.getEmail() + "\n" +
                "Address: " + visitor.getAddress();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("statusneo9@gmail.com"); // Change to your email

        mailSender.send(message);
    }


    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendVisitorData(String recipientEmail) throws MessagingException, IOException {
        List<Visitor> visitors = visitorRepository.findAll();
        byte[] excelData = excelGeneratorService.generateExcel(visitors);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(recipientEmail);
        helper.setSubject("Visitor Data Report");
        helper.setText("Attached is the visitor data report.");

        // Attach Excel file
        helper.addAttachment("Visitor_Report.xlsx", new ByteArrayResource(excelData));

        mailSender.send(message);
    }


}
