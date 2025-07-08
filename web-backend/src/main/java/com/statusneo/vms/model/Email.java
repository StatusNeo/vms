package com.statusneo.vms.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents an email message to be sent.
 *
 * @param from        The sender's email address.
 * @param to          The list of recipient email addresses.
 * @param subject     The subject of the email.
 * @param body        The body content of the email.
 * @param attachments The list of attachments (optional, may be null or empty).
 */
public record Email(
    String from,
    List<String> to,
    String subject,
    String body,
    List<Attachment> attachments
) {
    /**
     * Creates an Email for a single recipient, without attachments.
     */
    public static Email of(String from, String to, String subject, String body) {
        return new Email(from, List.of(to), subject, body, List.of());
    }
    /**
     * Creates an Email for multiple recipients, without attachments.
     */
    public static Email of(String from, List<String> to, String subject, String body) {
        return new Email(from, to, subject, body, List.of());
    }
    public Email {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        Objects.requireNonNull(subject, "subject must not be null");
        Objects.requireNonNull(body, "body must not be null");
        // attachments can be null (optional)
    }
} 