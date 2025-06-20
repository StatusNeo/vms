package com.statusneo.vms.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a one-time password (OTP) in the Visitor Management System.
 * OTPs are tied to specific Visit records and are used for visitor email validation.
 * OTPs can be resent up to twice for validation purposes.
 */
@Entity
@Table(name = "otp")
public class Otp {
    /**
     * Unique identifier for the OTP record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The email address to which the OTP was sent.
     */
    private String email;

    /**
     * The actual OTP code.
     */
    private String otp;

    /**
     * The time at which this OTP expires.
     */
    private LocalDateTime expirationTime;

    /**
     * The Visit record associated with this OTP.
     */
    @ManyToOne
    @JoinColumn(name = "visit_id")
    private Visit visit;

    /**
     * Counter tracking how many times this OTP has been resent.
     * OTPs can be resent up to twice for validation.
     */
    @Column(name = "resend_count", nullable = false)
    private Integer resendCount = 0;

    /**
     * Timestamp when the OTP record was created.
     * Automatically set when the record is persisted.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Otp(Long id, String email, String otp, LocalDateTime expirationTime, Visit visit, Integer resendCount, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.otp = otp;
        this.expirationTime = expirationTime;
        this.visit = visit;
        this.resendCount = resendCount;
        this.createdAt = createdAt;
    }

    public Otp() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (this.resendCount == null) {
            this.resendCount = 0;
        }
    }


    public Long getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public String getOtp() {
        return this.otp;
    }

    public LocalDateTime getExpirationTime() {
        return this.expirationTime;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public Integer getResendCount() {
        return resendCount;
    }

    public void setResendCount(Integer resendCount) {
        this.resendCount = resendCount;
    }

    /**
     * Increments the resend count by 1.
     * 
     * @return true if the OTP can still be resent (count < 2), false otherwise
     */
    public boolean incrementResendCount() {
        this.resendCount++;
        return this.resendCount <= 2; // Can be resent up to twice (total of 3 sends)
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Otp)) return false;
        Otp otp = (Otp) o;
        return Objects.equals(id, otp.id) &&
                Objects.equals(email, otp.email) &&
                Objects.equals(this.otp, otp.otp) &&
                Objects.equals(expirationTime, otp.expirationTime) &&
                Objects.equals(visit, otp.visit) &&
                Objects.equals(resendCount, otp.resendCount) &&
                Objects.equals(createdAt, otp.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, otp, expirationTime, visit, resendCount, createdAt);
    }

    public String toString() {
        return "Otp(id=" + this.getId() + 
               ", email=" + this.getEmail() + 
               ", otp=" + this.getOtp() + 
               ", expirationTime=" + this.getExpirationTime() + 
               ", visit=" + (this.getVisit() != null ? this.getVisit().getId() : "null") + 
               ", resendCount=" + this.getResendCount() + 
               ", createdAt=" + this.getCreatedAt() + ")";
    }
}
