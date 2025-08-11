package com.statusneo.vms.service;

import com.statusneo.vms.cache.EmployeeNameCache;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeNameCacheTest {

    private EmployeeNameCache cache;
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);

        List<Employee> testEmployees = Arrays.asList(
                new Employee("Anurag"), new Employee("Amit"),
                new Employee("Tarun"), new Employee("Anas"),
                new Employee("Disha")
        );
        when(employeeRepository.findAll()).thenReturn(testEmployees);

        cache = new EmployeeNameCache();
        cache.employeeRepository = employeeRepository;
        cache.initializeCache();
    }

    @Test
    void testGetEmployeeNamesByPrefix_A() {
        List<String> result = cache.getEmployeeNamesByPrefix("a");
        assertTrue(result.contains("anurag"));
        assertTrue(result.contains("amit"));
        assertTrue(result.contains("anas"));
        assertEquals(3, result.size());
    }

    @Test
    void testEmptyPrefixReturnsAllNames() {
        List<String> result = cache.getEmployeeNamesByPrefix("");
        assertTrue(result.contains("anurag"));
        assertTrue(result.contains("amit"));
        assertTrue(result.contains("tarun"));
        assertTrue(result.contains("anas"));
        assertTrue(result.contains("disha"));
        assertEquals(5, result.size());
    }

    @Test
    void testNonExistentPrefixReturnsEmpty() {
        List<String> result = cache.getEmployeeNamesByPrefix("z");
        assertTrue(result.isEmpty());
    }
}