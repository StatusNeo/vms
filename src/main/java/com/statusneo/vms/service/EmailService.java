package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitorRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VisitorRepository visitorRepository;


    @Autowired
    private ExcelService excelService;

    private String recipientEmail = "arshu.rashid.khan@gmail.com"; // Default recipient
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);


    public EmailService(JavaMailSender mailSender, VisitorRepository visitorRepository) {
        this.mailSender = mailSender;
        this.visitorRepository = visitorRepository;
    }

    @Async
    public void sendVisitorEmail(Visitor visitor) {
        String toEmail = "arshu.rashid.khan@gmail.com";  // Change this to the recipient email
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
        message.setFrom("arshu.rashid.khan@gmail.com");

        mailSender.send(message);
    }


    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

//    @Async
//    public void sendVisitorData(String recipientEmail) throws MessagingException, IOException {
//        List<Visitor> visitors = visitorRepository.findAll();
//        byte[] excelData = excelService.generateExcel(visitors);
//
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//        helper.setTo(recipientEmail);
//        helper.setSubject("Visitor Data Report");
//        helper.setText("Attached is the visitor data report.");
//
//        // Attach Excel file
//        helper.addAttachment("Visitor_Report.xlsx", new ByteArrayResource(excelData));
//
//        mailSender.send(message);
//    }

//    @Async
//    public void sendVisitorReport(String email) {
//        try {
//            List<Visitor> visitors = visitorRepository.findAll();
//            ByteArrayInputStream excelFile = ExcelService.generateVisitorExcel(visitors);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(email);
//            helper.setSubject("Visitor Data Report");
//            helper.setText("Please find attached the visitor report.");
//
//            InputStreamSource attachment = new ByteArrayResource(excelFile);
//            helper.addAttachment("Visitor_Report.xlsx", attachment);
//
//            mailSender.send(message);
//        } catch (MessagingException | IOException e) {
//            throw new RuntimeException("Failed to send email", e);
//        }
//
//    }

    public void sendEmailWithAttachment(String to, String subject, String text, byte[] attachment)
            throws MessagingException {
        logger.info("Sending email to {} with attachment of size: {} bytes", to, attachment.length);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            helper.addAttachment("visitors.xlsx", new ByteArrayResource(attachment));

            mailSender.send(message);
            logger.info("Email sent successfully to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}", to, e);
            throw e;
        }
    }
    public void setRecipientEmail(String email) {
        this.recipientEmail = email;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

}
