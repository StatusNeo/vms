package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PendingRegistrationService {
    private final Map<String, Visitor> pendingVisitors = new ConcurrentHashMap<>();
    private static final long EXPIRATION_TIME_MS = 15 * 60 * 1000; // 15 minutes

    public void savePendingVisitor(String email, Visitor visitor) {
        pendingVisitors.put(email, visitor);
    }

    public Visitor getAndRemove(String email) {
        Visitor visitor = pendingVisitors.get(email);
        if (visitor != null) {
            // Check if the registration attempt has expired
            if (System.currentTimeMillis() - visitor.getCreatedAt().getTime() > EXPIRATION_TIME_MS) {
                pendingVisitors.remove(email);
                return null;
            }
            pendingVisitors.remove(email);
            return visitor;
        }
        return null;
    }
}