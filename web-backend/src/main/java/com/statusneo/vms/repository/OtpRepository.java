
package com.statusneo.vms.repository;

import com.statusneo.vms.model.Otp;
import com.statusneo.vms.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    @Query("SELECT COUNT(o) > 0 FROM Otp o WHERE o.visit = :visit AND o.otp = :otpCode AND o.expirationTime > :currentTime ORDER BY o.createdAt DESC LIMIT 1")
    boolean isValidOtp(@Param("visit") Visit visit, @Param("otpCode") String otpCode, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT o FROM Otp o WHERE o.visit.id = :visitId ORDER BY o.createdAt DESC LIMIT 1")
    Optional<Otp> findFirstByVisitIdOrderByCreatedAtDesc(@Param("visitId") Long visitId);
    Optional<Otp> findFirstByVisitOrderByCreatedAtDesc(Visit visit);
}
