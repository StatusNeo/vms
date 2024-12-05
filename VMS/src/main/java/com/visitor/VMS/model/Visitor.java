package com.visitor.VMS.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "visitor_db")
public class Visitor {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phoneNumber;
    private String email;
    private String whomToMeet;
    private String otp;
    private Boolean isApproved = false;
    @Column(name = "visit_date")
    private LocalDateTime visitDate;
    private String imageUrl;
    
	public Visitor() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getWhomToMeet() {
		return whomToMeet;
	}
	public void setWhomToMeet(String whomToMeet) {
		this.whomToMeet = whomToMeet;
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
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	public Visitor(Long id, String name, String phoneNumber, String email, String whomToMeet, String otp,
			Boolean isApproved, LocalDateTime visitDate, String imageUrl) {
		super();
		this.id = id;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.whomToMeet = whomToMeet;
		this.otp = otp;
		this.isApproved = isApproved;
		this.visitDate = visitDate;
		this.imageUrl = imageUrl;
	}
	
	@Override
	public String toString() {
		return "Visitor [id=" + id + ", name=" + name + ", phoneNumber=" + phoneNumber + ", email=" + email
				+ ", whomToMeet=" + whomToMeet + ", otp=" + otp + ", isApproved=" + isApproved + ", visitDate="
				+ visitDate + ", imageUrl=" + imageUrl + "]";
	}
}
