package com.statusneo.vms.service;

import com.statusneo.vms.TestcontainersConfiguration;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.EmployeeDTO;
import com.statusneo.vms.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest
@Import({TestcontainersConfiguration.class})
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private GraphApiService graphApiService;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    public void shouldSaveNewEmployees() {
        // Arrange
        List<EmployeeDTO> employeeDTOs = List.of(
                new EmployeeDTO("Amit Jangra", "amit@example.com"),
                new EmployeeDTO("John Doe", "john@example.com")
        );

        when(graphApiService.getEmployees()).thenReturn(employeeDTOs);
        when(employeeRepository.existsByEmail("amit@example.com")).thenReturn(false);
        when(employeeRepository.existsByEmail("john@example.com")).thenReturn(false);

        // Act
        employeeService.syncEmployees();

        // Assert
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository, times(2)).save(captor.capture());

        List<Employee> savedEmployees = captor.getAllValues();

        assertEquals("Amit Jangra", savedEmployees.get(0).getName());
        assertEquals("amit@example.com", savedEmployees.get(0).getEmail());

        assertEquals("John Doe", savedEmployees.get(1).getName());
        assertEquals("john@example.com", savedEmployees.get(1).getEmail());
    }

    @Test
    public void shouldNotSaveIfEmailExistsOrNull() {
        // Arrange
        List<EmployeeDTO> employeeDTOs = List.of(
                new EmployeeDTO("Amit Jangra", "amit@example.com"),
                new EmployeeDTO("Anonymous", null), // invalid (null email)
                new EmployeeDTO("John Doe", "john@example.com")
        );

        when(graphApiService.getEmployees()).thenReturn(employeeDTOs);
        when(employeeRepository.existsByEmail("amit@example.com")).thenReturn(true); // already exists
        when(employeeRepository.existsByEmail("john@example.com")).thenReturn(false);

        // Act
        employeeService.syncEmployees();

        // Assert
        verify(employeeRepository, times(1)).save(any());
        verify(employeeRepository).save(argThat(emp ->
                emp.getName().equals("John Doe") && emp.getEmail().equals("john@example.com")));
    }
}