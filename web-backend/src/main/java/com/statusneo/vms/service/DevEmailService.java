package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class DevEmailService implements EmailService {

    private static final Logger log =
            LoggerFactory.getLogger(DevEmailService.class);

    @Override
    public boolean sendEmail(Email email) {
        log.info("[DEV EMAIL MOCK] Email payload: {}", email);
        return true;
    }
}