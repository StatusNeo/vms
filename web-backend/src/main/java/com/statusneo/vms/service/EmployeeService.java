/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.statusneo.vms.service;

import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.EmployeeDTO;
import com.statusneo.vms.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepository employeeRepository;
    private final GraphApiService graphApiService;

    public EmployeeService(EmployeeRepository employeeRepository, GraphApiService graphApiService) {
        this.employeeRepository = employeeRepository;
        this.graphApiService = graphApiService;
    }

    public List<Employee> searchEmployeesByName(String prefix) {
        logger.info("Searching employees with name starting with: {}", prefix);
        List<Employee> employees = employeeRepository.findByNameStartingWithIgnoreCase(prefix);
        logger.info("Found {} employees matching prefix '{}'", employees.size(), prefix);
        return employees;
    }

    public void syncEmployees() {
        List<EmployeeDTO> employees = graphApiService.getEmployees();

        for (EmployeeDTO dto : employees) {
            if (dto.getMail() != null && !employeeRepository.existsByEmail(dto.getMail())) {
                Employee employee = new Employee();
                employee.setName(dto.getDisplayName());
                employee.setEmail(dto.getMail());
                employeeRepository.save(employee);
            }
        }
    }
}