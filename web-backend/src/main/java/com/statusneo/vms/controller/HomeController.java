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
 * Unless required by applicable law or agreed in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.statusneo.vms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.statusneo.vms.cache.EmployeeNameCache;
import com.statusneo.vms.model.Employee;

/**
 * Expose host search endpoint backed by EmployeeNameCache so UI queries use
 * cache.
 */
@Controller
public class HomeController {

    private final EmployeeNameCache employeeNameCache;

    @Autowired
    public HomeController(EmployeeNameCache employeeNameCache /* , other deps if present */) {
        this.employeeNameCache = employeeNameCache;
    }

    /**
     * HTMX endpoint used by the host-search box.
     * Returns a small fragment containing search results. The fragment has
     * id="host-results"
     * so client-side JS/HTMX can reveal it after swap.
     *
     * Example request: GET /api/hosts/search?hostSearch=anas
     */
    @GetMapping("/api/hosts/search")
    public String searchHosts(@RequestParam(name = "hostSearch", required = false) String hostSearch, Model model) {
        List<Employee> results = employeeNameCache.search(hostSearch);
        model.addAttribute("hosts", results);
        // return Thymeleaf fragment - ensure fragment name "results" exists in template
        return "host-search-results :: results";
    }

    @GetMapping("/search-employees")
    public String employees(@RequestParam(value = "hostSearch", required = false) String hostSearch,
            @RequestParam(value = "employee", required = false) String employee,
            @RequestParam(value = "query", required = false) String query,
            Model model) {
        // Prefer hostSearch (used by index.jte), then employee, then query
        String q = (hostSearch != null && !hostSearch.isBlank()) ? hostSearch
                : ((employee != null && !employee.isBlank()) ? employee : (query == null ? "" : query));

        if (q.isBlank()) {
            return "employees";
        }

        List<Employee> employees = employeeNameCache.search(q);
        model.addAttribute("employees", employees);
        return "employees";
    }
}
