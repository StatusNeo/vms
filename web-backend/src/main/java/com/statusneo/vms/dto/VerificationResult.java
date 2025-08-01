package com.statusneo.vms.dto;

/**
 * DTO representing the result of OTP verification.
 *
 * @param success  Indicates if OTP verification was successful.
 * @param reattempt Indicates whether the user can reattempt OTP verification.
 * @param message  Message to be returned to the client.
 */
public record VerificationResult(boolean success, boolean reattempt, String message) {}
