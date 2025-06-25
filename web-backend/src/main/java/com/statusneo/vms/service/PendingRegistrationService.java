package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing temporary visitor registrations during the OTP verification process.
 * Uses an in-memory concurrent map to store pending registrations with expiration.
 *
 * @deprecated This service is scheduled for removal as it is not needed.
 */
@Deprecated(since = "1.0", forRemoval = true)
@Service
public class PendingRegistrationService {
    // Thread-safe map to store pending visitor registrations
    // Key: visitor email, Value: visitor details
    private final Map<String, Visitor> pendingVisitors = new ConcurrentHashMap<>();

    // Registration attempt expiration time (15 minutes in milliseconds)
    private static final long EXPIRATION_TIME_MS = 15 * 60 * 1000; // 15 minutes

    /**
     * Stores visitor details temporarily while waiting for OTP verification.
     * The stored data will expire after 15 minutes.
     *
     * @param email The visitor's email address (used as the key)
     * @param visitor The visitor details to store
     */
    public void savePendingVisitor(String email, Visitor visitor) {
        pendingVisitors.put(email, visitor);
    }

    /**
     * Retrieves and removes a pending visitor registration.
     * Also checks if the registration attempt has expired.
     *
     * @param email The email address to look up
     * @return The visitor details if found and not expired, null otherwise
     */
    public Visitor getAndRemove(String email) {
        Visitor visitor = pendingVisitors.get(email);
        if (visitor != null) {
            // Check if the registration attempt has expired (15 minutes)
            if (System.currentTimeMillis() - visitor.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() > EXPIRATION_TIME_MS) {
                pendingVisitors.remove(email);
                return null;
            }
            // Remove the visitor from pending registrations after retrieval
            pendingVisitors.remove(email);
            return visitor;
        }
        return null;
    }
}
