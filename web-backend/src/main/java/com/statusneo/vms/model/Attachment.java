package com.statusneo.vms.model;

/**
 * Represents an email attachment.
 * @param filename The name of the attachment file.
 * @param data The file data as a byte array.
 * @param contentType The MIME type of the attachment.
 */
public record Attachment(String filename, byte[] data, String contentType) {} 