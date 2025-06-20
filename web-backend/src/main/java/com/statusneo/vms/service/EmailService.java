package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;

public interface EmailService {

    void sendEmail(String toEmail, String subject, String body);

    void sendOtp(String email, String otp);

    void sendMeetingNotification(String employeeEmail, String visitorName);

    void sendVisitorEmail(Visitor visitor);

    void sendVisitorReport();

}