package com.statusneo.vms.web;

import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EmployeeConverter implements Converter<String, Employee> {

    private final EmployeeRepository employeeRepository;

    public EmployeeConverter(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        String trimmed = source.trim();
        try {
            Long id = Long.valueOf(trimmed);
            return employeeRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            // Try to resolve by exact case-insensitive name as a fallback
            return employeeRepository.findByNameIgnoreCase(trimmed).orElse(null);
        }
    }
}
