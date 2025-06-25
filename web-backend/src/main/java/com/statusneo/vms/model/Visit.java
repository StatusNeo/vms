package com.statusneo.vms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a visit record in the Visitor Management System.
 * This entity tracks visitor check-ins, approvals, and visit details.
 * Each visit can have multiple OTPs associated with it for visitor email validation.
 */
@Entity
@Table(name = "visiting_info")
public class Visit {
    /**
     * Unique identifier for the visit record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The visitor associated with this visit.
     */
    @OneToOne
    @JoinColumn(name = "visitor_id")
    private Visitor visitor;

    /**
     * The host employee that the visitor is meeting.
     */
    private String host;

    /**
     * OTPs associated with this visit for visitor email validation.
     * A visit can have multiple OTPs if they need to be resent.
     */
    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Otp> otps = new ArrayList<>();

    /**
     * Flag indicating whether the visit has been approved by the host.
     */
    private Boolean isApproved = false;

    /**
     * Date and time of the visit.
     */
    @Column(name = "visit_date")
    private LocalDateTime visitDate;

    /**
     * Default constructor required by JPA.
     */
    public Visit() {
    }

    /**
     * Constructs a new Visit with the specified details.
     *
     * @param id The unique identifier
     * @param visitor The visitor associated with this visit
     * @param host The host employee
     * @param isApproved The approval status
     * @param visitDate The date and time of the visit
     */
    public Visit(Long id, Visitor visitor, String host, Boolean isApproved, LocalDateTime visitDate) {
        this.id = id;
        this.visitor = visitor;
        this.host = host;
        this.isApproved = isApproved;
        this.visitDate = visitDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Visitor getVisitor() {
        return visitor;
    }

    public void setVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<Otp> getOtps() {
        return otps;
    }

    public void setOtps(List<Otp> otps) {
        this.otps = otps;
    }

    /**
     * The employee that the visitor is scheduled to meet.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employeeToMeet;

    public Employee getEmployeeToMeet() {
        return employeeToMeet;
    }

    public void setEmployeeToMeet(Employee employeeToMeet) {
        this.employeeToMeet = employeeToMeet;
    }

    /**
     * Adds an OTP to this visit and sets the visit reference in the OTP.
     *
     * @param otp The OTP to add
     */
    public void addOtp(Otp otp) {
        otps.add(otp);
        otp.setVisit(this);
    }

    /**
     * Removes an OTP from this visit.
     *
     * @param otp The OTP to remove
     */
    public void removeOtp(Otp otp) {
        otps.remove(otp);
        otp.setVisit(null);
    }

    /**
     * Gets the most recent OTP for this visit.
     *
     * @return The most recent OTP, or null if no OTPs exist
     */
    public Otp getLatestOtp() {
        if (otps.isEmpty()) {
            return null;
        }
        return otps.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .findFirst()
                .orElse(null);
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public LocalDateTime getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDateTime visitDate) {
        this.visitDate = visitDate;
    }

    @Override
    public String toString() {
        Otp latestOtp = getLatestOtp();
        String otpValue = latestOtp != null ? latestOtp.getOtp() : "null";
        return "VisitingInfo [id=" + id + ", visitor=" + visitor + ", host=" + host +
                ", latestOtp=" + otpValue + ", otpCount=" + otps.size() +
                ", isApproved=" + isApproved + ", visitDate=" + visitDate + "]";
    }
}
