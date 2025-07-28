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
package com.statusneo.vms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index"; // This assumes index.html is in the static directory
    }

    @GetMapping("/search-employees")
    public String employees(@RequestParam(value = "query", required = false) String query, Model model) {
        if (query == null || query.isEmpty()) {
            // Do not add anything to the model and return the view
            return "employees";
        }

        List<String> employees = Arrays.asList("Alice", "Bob", "Charlie", "Diana", "Edward");

        // Filter employees if query is not null or empty
        employees = employees.stream()
                .filter(name -> name.toLowerCase().contains(query.toLowerCase()))
                .toList();
        // Add the filtered list to the model
        model.addAttribute("employees", employees);

        return "employees";
    }
}
