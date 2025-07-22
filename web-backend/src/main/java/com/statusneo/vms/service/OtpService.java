package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Otp;
import com.statusneo.vms.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Value("${visitor.system.otp.subject}")
    private String otpSubject;

    private static final int OTP_EXPIRATION_MINUTES = 10;
    private final Map<String, Boolean> verifiedEmails = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> verificationTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Integer> otpAttempts = new ConcurrentHashMap<>();
    private static final int MAX_OTP_ATTEMPTS = 2;

    public String generatedOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000); // 6-digit OTP
    }

    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    private void sendOtp(String email, String otp) {
        emailService.sendEmail(Email.of("fromEmail", email, "Your OTP", "Your OTP is: " + otp));
    }

    public void sendOtp(String email) {
        String otp = generateOtp();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpirationTime(expirationTime);

        otpRepository.save(otpEntity);
        emailService.sendEmail(Email.of(email, email, otpSubject, "Your OTP is: " + otp));
    }

    public Optional<Otp> getLatestOtpByEmail(String email) {
        List<Otp> otps = otpRepository.findByEmailOrdered(email);
        return otps.isEmpty() ? Optional.empty() : Optional.of(otps.getFirst());
    }
    /**
     * Resends a new OTP to the given email if at least 2 minutes have passed since
     * the last OTP was sent.
     *
     * @param email The email to resend the OTP to.
     * @return true if OTP was resent, false if not enough time has passed.
     */
    public boolean canResendOtp(String email) {
        Optional<Otp> latestOtp = getLatestOtpByEmail(email);
        if (latestOtp.isPresent()) {
            LocalDateTime lastSent = latestOtp.get().getExpirationTime().minusMinutes(OTP_EXPIRATION_MINUTES);
            if (lastSent.plusMinutes(2).isAfter(LocalDateTime.now())) {
                // Less than 2 minutes since last OTP sent
                return false;
            }
        }
        sendOtp(email);
        return true;
    }

    public boolean validateOtp(String email, String otp) {
        int attempts = otpAttempts.getOrDefault(email, 0);
        if (attempts >= MAX_OTP_ATTEMPTS) {
            return false;
        }

        Optional<Otp> latestOtp = getLatestOtpByEmail(email);
        boolean valid = latestOtp.filter(value -> !value.getExpirationTime().isBefore(LocalDateTime.now()) &&
                value.getOtp().equals(otp)).isPresent();
        if (!valid) {
            otpAttempts.put(email, attempts + 1);
        } else {
            otpAttempts.remove(email);
        }
        return valid;
    }

    public void markEmailAsVerified(String email) {
        otpAttempts.remove(email);
        verifiedEmails.put(email, true);
        verificationTimestamps.put(email, LocalDateTime.now());
    }

    public boolean isEmailVerified(String email) {
        LocalDateTime timestamp = verificationTimestamps.get(email);
        if (timestamp != null && timestamp.plusMinutes(15).isBefore(LocalDateTime.now())) {
            clearVerifiedEmail(email);
            return false;
        }
        return verifiedEmails.getOrDefault(email, false);
    }

    public void clearVerifiedEmail(String email) {
        otpAttempts.remove(email);
    }

    /**
     * Returns true if the visitor has reached the max OTP attempts.
     */
    public boolean hasExceededOtpAttempts(String email) {
        return otpAttempts.getOrDefault(email, 0) >= MAX_OTP_ATTEMPTS;
    }

}