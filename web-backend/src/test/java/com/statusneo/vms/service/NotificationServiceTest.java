package com.statusneo.vms.service;

import com.statusneo.vms.config.EmailProperties;
import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.util.EmailTemplateProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    private EmailService emailService;
    private EmailTemplateProcessor templateProcessor;
    private EmailProperties emailProperties;
    private EmailProperties.VisitorConfirmation visitorConfirmationProps;
    private EmailProperties.HostNotification hostNotificationProps;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        templateProcessor = mock(EmailTemplateProcessor.class);

        emailProperties = mock(EmailProperties.class);
        visitorConfirmationProps = mock(EmailProperties.VisitorConfirmation.class);
        hostNotificationProps = mock(EmailProperties.HostNotification.class);

        when(emailProperties.getSender()).thenReturn("noreply@company.com");

        when(emailProperties.getVisitorConfirmation()).thenReturn(visitorConfirmationProps);
        when(visitorConfirmationProps.getSubject()).thenReturn("Registration Successful");
        when(visitorConfirmationProps.getTemplate()).thenReturn("visitorConfirmation.txt");

        when(emailProperties.getHostNotification()).thenReturn(hostNotificationProps);
        when(hostNotificationProps.getSubject()).thenReturn("Visitor Alert");
        when(hostNotificationProps.getTemplate()).thenReturn("hostNotification.txt");

        notificationService = new NotificationService(emailService, templateProcessor, emailProperties);
    }

    @Test
    void testSendVisitorConfirmationEmail() {
        Visitor visitor = new Visitor();
        visitor.setName("John Doe");
        visitor.setEmail("john.doe@example.com");

        String expectedTemplate = """
                Dear John Doe,
                Your visit registration was successful.

                Name: John Doe
                Email: john.doe@example.com

                Thank you!
                """;

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("visitorName", "John Doe");
        placeholders.put("visitorEmail", "john.doe@example.com");

        when(templateProcessor.loadTemplate(eq("visitorConfirmation.txt"), any())).thenReturn(expectedTemplate);
        when(emailService.sendEmail(any(Email.class))).thenReturn(true);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        notificationService.sendVisitorConfirmationEmail(visitor);

        verify(emailService).sendEmail(emailCaptor.capture());
        Email sentEmail = emailCaptor.getValue();

        assertEquals("noreply@company.com", sentEmail.from());
        assertEquals(List.of("john.doe@example.com"), sentEmail.to());
        assertEquals("Registration Successful", sentEmail.subject());
        assertEquals(expectedTemplate.strip(), sentEmail.body().strip());
    }

    @Test
    void testSendHostNotification() {
        Visitor visitor = new Visitor();
        visitor.setName("Jane Smith");
        visitor.setEmail("jane.smith@example.com");

        Employee host = new Employee();
        host.setName("Host Person");
        host.setEmail("host@example.com");

        String expectedTemplate = """
                Dear Host Person,
                You have a new visitor coming to meet you.
                Visitor Name: Jane Smith
                Email: jane.smith@example.com
                Please be ready to receive them.
                """;

        when(templateProcessor.loadTemplate(eq("hostNotification.txt"), any())).thenReturn(expectedTemplate);
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