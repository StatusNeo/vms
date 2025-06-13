package com.statusneo.vms.repository;

import com.statusneo.vms.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findFirstByEmailOrderByCreatedAtDesc(String email);

    List<Otp> findByEmailOrderByCreatedAtDesc(String email);

    void deleteByExpirationTimeBefore(LocalDateTime expiryTime);

    Optional<Otp> findByEmailAndOtp(String email, String otp);
    
    Optional<Otp> findByEmailAndOtpAndExpirationTimeAfter(String email, String otp, LocalDateTime currentTime);
    
    @Query("SELECT o FROM Otp o WHERE o.email = :email ORDER BY o.createdAt DESC")
    List<Otp> findByEmailOrdered(@Param("email") String email);

    List<Otp> findByEmailOrderByExpirationTimeDesc(String email);

}