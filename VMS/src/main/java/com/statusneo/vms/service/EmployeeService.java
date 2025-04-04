package com.statusneo.vms.service;

import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> searchEmployeesByName(String prefix) {
        logger.info("Searching employees with name starting with: {}", prefix);
        List<Employee> employees = employeeRepository.findByNameStartingWithIgnoreCase(prefix);
        logger.info("Found {} employees matching prefix '{}'", employees.size(), prefix);
        return employees;
    }
}