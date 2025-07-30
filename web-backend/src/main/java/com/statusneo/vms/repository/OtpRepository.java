
package com.statusneo.vms.repository;

import com.statusneo.vms.model.Otp;
import com.statusneo.vms.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    boolean existsByVisitAndOtpAndExpirationTimeAfter(Visit visit, String otp, LocalDateTime currentTime);

    Optional<Otp> findFirstByVisitIdOrderByCreatedAtDesc(Long visitId);

    Optional<Otp> findFirstByVisitOrderByCreatedAtDesc(Visit visit);
}
