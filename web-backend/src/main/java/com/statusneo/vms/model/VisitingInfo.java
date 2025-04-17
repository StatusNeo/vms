package com.statusneo.vms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visiting_info")
public class VisitingInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "visitor_id")
    private Visitor visitor;

    private String host;
    private String otp;
    private Boolean isApproved = false;

    @Column(name = "visit_date")
    private LocalDateTime visitDate;

    public VisitingInfo() {
    }

    public VisitingInfo(Long id, Visitor visitor, String host, String otp, Boolean isApproved, LocalDateTime visitDate) {
        this.id = id;
        this.visitor = visitor;
        this.host = host;
        this.otp = otp;
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

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
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
        return "VisitingInfo [id=" + id + ", visitor=" + visitor + ", host=" + host + ", otp=" + otp +
                ", isApproved=" + isApproved + ", visitDate=" + visitDate + "]";
    }
}
