package com.statusneo.vms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);
    }

    public void sendMeetingNotification(String email, String whomToMeet) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Meeting Notification");
        message.setText("You have a visitor to meet: " + whomToMeet);
        mailSender.send(message);
    }
}
