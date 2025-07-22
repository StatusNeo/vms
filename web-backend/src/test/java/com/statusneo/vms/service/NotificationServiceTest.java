package com.statusneo.vms.service;

import com.statusneo.vms.config.EmailProperties;
import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.util.EmailTemplateProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {
    private EmailService emailService;
    private EmailTemplateProcessor templateProcessor;
    private EmailProperties emailProperties;
    private NotificationService notificationService;
    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        templateProcessor = mock(EmailTemplateProcessor.class);
        emailProperties = mock(EmailProperties.class);
        when(emailProperties.getSender()).thenReturn("noreply@company.com");
        notificationService = new NotificationService(emailService, templateProcessor, emailProperties);
    }

    @Test
    void testSendVisitorConfirmationEmail() {
        Visitor visitor = new Visitor();
        visitor.setName("John Doe");
        visitor.setEmail("john.doe@example.com");

        when(emailService.sendEmail(any(Email.class))).thenReturn(true);
        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

        notificationService.sendVisitorConfirmationEmail(visitor);

        verify(emailService).sendEmail(emailCaptor.capture());
        Email sentEmail = emailCaptor.getValue();

        assertEquals("noreply@company.com", sentEmail.from());
        assertEquals(List.of("john.doe@example.com"), sentEmail.to());
        assertEquals("Registration Successful", sentEmail.subject());

        String expectedBody = """
                Dear John Doe,
                Your visit registration was successful.

                Name: John Doe
                Email: john.doe@example.com

                Thank you!
                """;

        assertEquals(expectedBody.strip(), sentEmail.body().strip());
    }

    @Test
    void testSendHostNotification() {
        Visitor visitor = new Visitor();
        visitor.setName("Jane Smith");
        visitor.setEmail("jane.smith@example.com");
        String expectedTemplate = """
                Dear Host Person,
                You have a new visitor coming to meet you.
                Visitor Name: Jane Smith
                Email: jane.smith@example.com
                Please be ready to receive them.
                """;

        when(templateProcessor.loadTemplate(eq("hostNotification.txt"), any())).thenReturn(expectedTemplate);

        Employee host = new Employee();
        host.setName("Host Person");
        host.setEmail("host@example.com");

        when(emailService.sendEmail(any(Email.class))).thenReturn(true);
        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

        notificationService.sendHostNotification(visitor, host);

        verify(emailService).sendEmail(emailCaptor.capture());
        Email sentEmail = emailCaptor.getValue();

        assertEquals("noreply@company.com", sentEmail.from());
        assertEquals(List.of("host@example.com"), sentEmail.to());
        assertEquals("Visitor Alert", sentEmail.subject());
        assertEquals(expectedTemplate.strip(), sentEmail.body().strip());
    }

}