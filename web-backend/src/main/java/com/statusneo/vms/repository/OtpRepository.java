
package com.statusneo.vms.repository;

import com.statusneo.vms.model.Otp;
import com.statusneo.vms.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Otp entities.
 * Provides methods for finding OTPs by email, visit, and for counting OTPs.
 */
@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    /**
     * Finds all OTPs for a given email, ordered by creation time (newest first).
     *
     * @param email The email to search for
     * @return List of OTPs for the email, ordered by creation time
     */
    @Query("SELECT o FROM Otp o WHERE o.email = :email ORDER BY o.createdAt DESC")
    List<Otp> findByEmailOrdered(@Param("email") String email);

    /**
     * Finds all OTPs for a specific visit, ordered by creation time (newest first).
     *
     * @param visit The visit to search for
     * @return List of OTPs for the visit, ordered by creation time
     */
    @Query("SELECT o FROM Otp o WHERE o.visit = :visit ORDER BY o.createdAt DESC")
    List<Otp> findByVisitOrdered(@Param("visit") Visit visit);

    /**
     * Finds the most recent OTP for a specific visit.
     *
     * @param visit The visit to search for
     * @return Optional containing the most recent OTP, or empty if none exists
     */
    @Query("SELECT o FROM Otp o WHERE o.visit = :visit ORDER BY o.createdAt DESC")
    Optional<Otp> findLatestByVisit(@Param("visit") Visit visit);

    /**
     * Counts the number of OTPs generated for a specific visit.
     *
     * @param visit The visit to count OTPs for
     * @return The number of OTPs for the visit
     */
    long countByVisit(Visit visit);
}
