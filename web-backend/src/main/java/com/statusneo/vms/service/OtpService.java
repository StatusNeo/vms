package com.statusneo.vms.service;

import com.statusneo.vms.model.Otp;
import com.statusneo.vms.model.Visit;
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

/**
 * Service responsible for managing One-Time Passwords (OTP) for visitor verification.
 * Handles OTP generation, validation, and email verification status tracking.
 */
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

    // Remove duplicate method generatedOtp() and keep only generateOtp()
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }


    /**
     * Generates a new OTP and sends it to the specified email address.
     * This method is maintained for backward compatibility.
     *
     * @param email The email address to send the OTP to
     * @deprecated Use {@link #sendOtp(String, Visit)} instead to associate the OTP with a Visit
     */


    /**
     * Generates a new OTP, associates it with the specified Visit, and sends it to the specified email address.
     * This method tracks OTP resends and limits them to a maximum of 2 resends (3 total sends).
     *
     * @param email The email address to send the OTP to
     * @param visit The Visit to associate the OTP with
     * @return true if the OTP was sent successfully, false if the maximum resend limit has been reached
     */
    public boolean sendOtp(String email, Visit visit) {
        // Check if we've already sent the maximum number of OTPs for this visit
        long otpCount = otpRepository.countByVisit(visit);
        if (otpCount >= 3) { // Initial send + 2 resends = 3 total
            return false;
        }

        String otp = generateOtp();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpirationTime(expirationTime);
        otpEntity.setVisit(visit);
        otpEntity.setResendCount((int)otpCount); // 0 for first send, 1 for first resend, 2 for second resend

        otpRepository.save(otpEntity);
        visit.addOtp(otpEntity);

        emailService.sendEmail(email, otpSubject, "Your OTP is: " + otp);
        return true;
    }

    /**
     * Retrieves the latest OTP for a given email address.
     *
     * @param email The email address to look up
     * @return Optional containing the latest OTP entity if found
     */
    public Optional<Otp> getLatestOtpByEmail(String email) {
        List<Otp> otps = otpRepository.findByEmailOrdered(email);
        return otps.isEmpty() ? Optional.empty() : Optional.of(otps.get(0));
    }

    /**
     * Retrieves the latest OTP for a given visit.
     *
     * @param visit The visit to look up
     * @return Optional containing the latest OTP entity if found
     */
    public Optional<Otp> getLatestOtpByVisit(Visit visit) {
        return otpRepository.findLatestByVisit(visit);
    }

    /**
     * Validates an OTP for a given email address.
     * This method is maintained for backward compatibility.
     *
     * @param email The email address associated with the OTP
     * @param otp The OTP to validate
     * @return true if the OTP is valid and not expired, false otherwise
     * @deprecated Use {@link #validateOtp(Visit, String)} instead to validate OTPs associated with a Visit
     */


    /**
     * Validates an OTP for a given visit.
     *
     * @param visit The visit associated with the OTP
     * @param otp The OTP to validate
     * @return true if the OTP is valid and not expired, false otherwise
     */
    public boolean validateOtp(Visit visit, String otp) {
        Optional<Otp> latestOtp = getLatestOtpByVisit(visit);
        if (latestOtp.isEmpty()) {
            return false;
        }
        return !latestOtp.get().getExpirationTime().isBefore(LocalDateTime.now()) &&
                latestOtp.get().getOtp().equals(otp);
    }

    /**
     * Checks if an OTP can be resent for a given visit.
     * OTPs can be resent up to twice (for a total of 3 sends including the initial send).
     *
     * @param visit The visit to check
     * @return true if an OTP can be resent, false if the maximum resend limit has been reached
     */
    public boolean canResendOtp(Visit visit) {
        return otpRepository.countByVisit(visit) < 3;
    }

    /**
     * Marks an email address as verified for a limited time period.
     *
     * @param email The email address to mark as verified
     */
    public void markEmailAsVerified(String email) {
        verifiedEmails.put(email, true);
        verificationTimestamps.put(email, LocalDateTime.now());
    }

    /**
     * Checks if an email address is currently verified.
     * Verification status expires after 15 minutes.
     *
     * @param email The email address to check
     * @return true if the email is verified and not expired, false otherwise
     */
    public boolean isEmailVerified(String email) {
        LocalDateTime timestamp = verificationTimestamps.get(email);
        if (timestamp != null && timestamp.plusMinutes(15).isBefore(LocalDateTime.now())) {
            clearVerifiedEmail(email);
            return false;
        }
        return verifiedEmails.getOrDefault(email, false);
    }

    /**
     * Clears the verification status for an email address.
     *
     * @param email The email address to clear verification for
     */
    public void clearVerifiedEmail(String email) {
        verifiedEmails.remove(email);
        verificationTimestamps.remove(email);
    }
}
