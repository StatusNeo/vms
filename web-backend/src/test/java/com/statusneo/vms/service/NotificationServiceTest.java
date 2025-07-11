package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {
    private EmailService emailService;
    private EmployeeRepository employeeRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        employeeRepository = mock(EmployeeRepository.class);
        notificationService = new NotificationService(emailService, employeeRepository);
    }

    @Test
    void testSendVisitorConfirmationEmail() {
        Visitor visitor = new Visitor();
        visitor.setName("John Doe");
        visitor.setEmail("john.doe@example.com");

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        when(emailService.sendEmail(any(Email.class))).thenReturn(true);

        notificationService.sendVisitorConfirmationEmail(visitor);

        verify(emailService, times(1)).sendEmail(emailCaptor.capture());
        Email sentEmail = emailCaptor.getValue();

        assertEquals("noreply@company.com", sentEmail.from());
        assertEquals(List.of("john.doe@example.com"), sentEmail.to());
        assertEquals("Registration Successful", sentEmail.subject());
        assert(sentEmail.body().contains("John Doe"));
    }

    @Test
    void testSendHostNotification_WhenHostExists() {
        Visitor visitor = new Visitor();
        visitor.setName("Jane Smith");
        visitor.setEmail("jane.smith@example.com");

        Employee host = new Employee();
        host.setName("Host Person");
        host.setEmail("host@example.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(host));
        when(emailService.sendEmail(any(Email.class))).thenReturn(true);
        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

        notificationService.sendHostNotification(visitor, 1L);

        verify(emailService, times(1)).sendEmail(emailCaptor.capture());
        Email sentEmail = emailCaptor.getValue();

        assertEquals("noreply@company.com", sentEmail.from());
        assertEquals(List.of("host@example.com"), sentEmail.to());
        assertEquals("Visitor Alert", sentEmail.subject());
        assert(sentEmail.body().contains("Jane Smith"));
        assert(sentEmail.body().contains("Host Person"));
    }

    @Test
    void testSendHostNotification_WhenHostNotFound() {
        Visitor visitor = new Visitor();
        visitor.setName("Visitor");
        visitor.setEmail("visitor@example.com");

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        notificationService.sendHostNotification(visitor, 99L);

        verify(emailService, never()).sendEmail(any());
    }

}
