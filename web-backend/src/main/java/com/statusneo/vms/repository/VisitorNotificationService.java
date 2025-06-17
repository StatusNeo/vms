package com.statusneo.vms.repository;
import com.statusneo.vms.model.Visitor;

public interface VisitorNotificationService {
    void sendVisitorEmail(Visitor visitor);
}

