package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visitor;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    private EmailService emailService;
    private TemplateEngine templateEngine;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        templateEngine = mock(TemplateEngine.class);
        notificationService = new NotificationService(emailService, templateEngine);
    }

    @Test
    void testSendVisitorConfirmationEmail() {
        Visitor visitor = new Visitor();
        visitor.setName("Anurag");
        visitor.setEmail("anu.sharma@example.com");

        String renderedBody = """
                Dear Anurag,
                Your visit registration was successful.

                Name: Anurag
                Email: anu.sharma@example.com

                Thank you!
                """;

        doAnswer(invocation -> {
            StringOutput output = invocation.getArgument(2);
            output.writeContent(renderedBody);
            return null;
        }).when(templateEngine).render(eq("visitorConfirmation.jte"), any(Map.class), any(StringOutput.class));

        when(emailService.sendEmail(any(Email.class))).thenReturn(true);
        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

        notificationService.sendVisitorConfirmationEmail(visitor);

        verify(emailService, times(1)).sendEmail(emailCaptor.capture());
        Email sentEmail = emailCaptor.getValue();

        assertEquals("noreply@company.com", sentEmail.from());
        assertEquals(List.of("anu.sharma@example.com"), sentEmail.to());
        assertEquals("Registration Successful", sentEmail.subject());
        assertEquals(renderedBody.strip(), sentEmail.body().strip());
    }

    @Test
    void testSendHostNotification() {
        Visitor visitor = new Visitor();
        visitor.setName("Vikas");
        visitor.setEmail("vikas.gupta@example.com");

        Employee host = new Employee();
        host.setName("Arun");
        host.setEmail("arun@example.com");

        String renderedBody = """
                Dear Arun,
                You have a new visitor coming to meet you.

                Visitor Name: Vikas
                Email: vikas.gupta@example.com

                Please be ready to receive them.
                """;

        doAnswer(invocation -> {
            StringOutput output = invocation.getArgument(2);
            output.writeContent(renderedBody);
            return null;
        }).when(templateEngine).render(eq("hostNotification.jte"), any(Map.class), any(StringOutput.class));

        when(emailService.sendEmail(any(Email.class))).thenReturn(true);
        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

        notificationService.sendHostNotification(visitor, host);

        verify(emailService, times(1)).sendEmail(emailCaptor.capture());
        Email sentEmail = emailCaptor.getValue();

        assertEquals("noreply@company.com", sentEmail.from());
        assertEquals(List.of("arun@example.com"), sentEmail.to());
        assertEquals("Visitor Alert", sentEmail.subject());
        assertEquals(renderedBody.strip(), sentEmail.body().strip());
    }
}