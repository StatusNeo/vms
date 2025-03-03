package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

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
        message.setFrom("arshu.rashid.khan@gmail.com"); // Change to your email

        mailSender.send(message);
    }
}
