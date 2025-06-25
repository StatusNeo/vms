package com.statusneo.vms.controller;

import com.statusneo.vms.model.AzureADUser;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.service.EmployeeService;
import com.statusneo.vms.service.UserSearchService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
public class AutocompleteController {
    private final UserSearchService userSearchService;
    private Logger logger;
    @Autowired
    private EmployeeService employeeService;

    public AutocompleteController(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @GetMapping("/autocomplete")
    public Set<AzureADUser> autocomplete(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        return userSearchService.searchUsers(query, limit);
    }


    @GetMapping("/search")
    public String searchEmployees(@RequestParam("employee") String query, Model model) {
        logger.info("Received search request for employee: {}", query);
        List<Employee> employees = employeeService.searchEmployeesByName(query);
        model.addAttribute("employees", employees);
        return "fragments/employee-search";
    }

    @GetMapping("/search-employees")
    public String employees(@RequestParam(value = "query", required = false) String query, Model model) {
        if (query == null || query.isEmpty()) {
            // Do not add anything to the model and return the view
            return "fragments/employees";
        }

        List<String> employees = Arrays.asList("Alice", "Bob", "Charlie", "Diana", "Edward");

        // Filter employees if query is not null or empty
        employees = employees.stream()
                .filter(name -> name.toLowerCase().contains(query.toLowerCase()))
                .toList();
        // Add the filtered list to the model
        model.addAttribute("employees", employees);

        return "fragments/employees";
    }

}
