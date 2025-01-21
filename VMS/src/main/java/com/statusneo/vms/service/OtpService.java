package com.statusneo.vms.service;

import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class OtpService {
	
	public String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000); // 6-digit OTP
    }
    
    public boolean verifyOtp(String enteredOtp, String generatedOtp) {
        return enteredOtp.equals(generatedOtp);
    }
}
