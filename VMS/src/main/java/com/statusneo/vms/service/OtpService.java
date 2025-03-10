package com.statusneo.vms.service;

import com.statusneo.vms.model.Otp;
import com.statusneo.vms.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
//
//@Service
//public class OtpService {
//
//	public String generateOtp() {
//        return String.valueOf(new Random().nextInt(900000) + 100000); // 6-digit OTP
//    }
//
//    public boolean verifyOtp(String enteredOtp, String generatedOtp) {
//        return enteredOtp.equals(generatedOtp);
//    }
//}
@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    private static final int OTP_EXPIRATION_MINUTES = 5;

    public String generatedOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000); // 6-digit OTP
    }

    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    public void sendOtp(String email) {
        String otp = generateOtp();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpirationTime(expirationTime);

        otpRepository.save(otpEntity);

        // Send OTP via email
        emailService.sendEmail(email, "Your OTP Code", "Your OTP is: " + otp);
    }

    public boolean validateOtp(String email, String otp) {
        Optional<Otp> latestOtp = getLatestOtpByEmail(email);
        if (latestOtp.isEmpty()) {
            throw new IllegalArgumentException("No OTP found for the given email.");
        }
        return latestOtp.get().getOtp().equals(otp);
    }

    public Optional<Otp> getLatestOtpByEmail(String email) {
        List<Otp> otps = otpRepository.findByEmailOrdered(email);
        return otps.isEmpty() ? Optional.empty() : Optional.of(otps.get(0)); // Return the latest OTP
    }
}
