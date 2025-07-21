package com.statusneo.vms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Represents a visitor in the Visitor Management System.
 * This entity stores information about individuals visiting the premises.
 */
@Entity
@Table(name = "visitor")
public class Visitor {

	private static final String PHONE_NUMBER_REGEX = "^\\d{10}$";


	/**
	 * Unique identifier for the visitor.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Full name of the visitor.
	 */
	private String name;

	/**
	 * Contact phone number of the visitor.
	 */
	private String phoneNumber;

	/**
	 * Email address of the visitor, used for communication and OTP verification.
	 */
	private String email;

	/**
	 * Physical address of the visitor.
	 */
	private String address;

	/**
	 * Path to the visitor's profile picture stored in the system.
	 */
	@Column(name = "picture_path")
	private String picturePath;

	@ManyToOne
	@JoinColumn(name = "host_id", referencedColumnName = "id")
	private Employee host;

	// getters & setters
	public Employee getHost() {
		return host;
	}

	public void setHost(Employee host) {
		this.host = host;
	}

	/**
	 * Default constructor required by JPA.
	 */
	public Visitor() {
	}

	/**
	 * Constructs a new Visitor with the specified details.
	 *
	 * @param id The unique identifier
	 * @param name The visitor's full name
	 * @param phoneNumber The visitor's phone number
	 * @param email The visitor's email address
	 * @param address The visitor's physical address
	 */
	public Visitor(Long id, String name, String phoneNumber, String email, String address) {
		this.id = id;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.address = address;
	}

	/**
	 * Timestamp when the visitor record was created.
	 * Automatically set when the record is persisted.
	 */
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/**
	 * Sets the creation timestamp before persisting the entity.
	 */
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public LocalDateTime getCreatedAt(){
		return createdAt;
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
		if (phoneNumber == null || !phoneNumber.matches(PHONE_NUMBER_REGEX)) {
			throw new IllegalArgumentException("Phone number must be exactly 10 digits and numeric");
		}
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPicturePath() {
		return picturePath;
	}
	public void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
	}

	@Override
	public String toString() {
		return "Visitor{" +
				"id=" + id +
				", name='" + name + '\'' +
				", phoneNumber='" + phoneNumber + '\'' +
				", email='" + email + '\'' +
				", address='" + address + '\'' +
				", picturePath='" + picturePath + '\'' +
				'}';
	}

}
