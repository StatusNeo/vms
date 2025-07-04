package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;

public interface EmailService {
    /**
     * Sends an email using the provided Email object.
     *
     * @param email Email object containing all necessary fields
     * @return true if the email was sent successfully, false otherwise
     */
    boolean sendEmail(Email email);
}
