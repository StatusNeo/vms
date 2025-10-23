package com.statusneo.vms.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;
import com.statusneo.vms.util.TrieNode;

import jakarta.annotation.PostConstruct;

@Component
public class EmployeeNameCache {

    private static final int MAX_SUGGESTIONS = 10;

    private final TrieNode root = new TrieNode();
    private final EmployeeRepository employeeRepository;

    public EmployeeNameCache(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @PostConstruct
    public void initializeCache() {
        clear();
        List<Employee> allEmployees = employeeRepository.findAll();
        for (Employee employee : allEmployees) {
            if (employee.getName() != null && !employee.getName().isBlank()) {
                insert(employee.getName());
            }
        }
    }

    public void insert(String name) {
        if (name == null || name.isBlank()) return;
        TrieNode node = root;
        for (char c : name.toLowerCase().toCharArray()) {
            node = node.getChildren().computeIfAbsent(c, k -> new TrieNode());
        }
        node.setEndOfWord(true);
        node.addOriginal(name);
    }

    public List<String> getEmployeeNamesByPrefix(String prefix) {
        if (prefix == null) prefix = "";
        TrieNode node = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            node = node.getChildren().get(c);
            if (node == null) return Collections.emptyList();
        }

        // Use LinkedHashSet to preserve insertion order and avoid duplicates
        Set<String> results = new LinkedHashSet<>();
        collectNames(node, results);

        // If prefix is empty, we may have many results - limit to MAX_SUGGESTIONS
        List<String> list = new ArrayList<>(results);
        if (list.size() > MAX_SUGGESTIONS) {
            return list.subList(0, MAX_SUGGESTIONS);
        }
        return list;
    }

    private void collectNames(TrieNode node, Set<String> results) {
        if (results.size() >= MAX_SUGGESTIONS) return;
        if (node.isEndOfWord()) {
            // add originals for this terminal node
            for (String orig : node.getOriginals()) {
                if (results.size() >= MAX_SUGGESTIONS) break;
                results.add(orig);
            }
        }

        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            if (results.size() >= MAX_SUGGESTIONS) break;
            collectNames(entry.getValue(), results);
        }
    }

    public void clear() {
        root.getChildren().clear();
    }

    public void bulkInsert(Collection<String> names) {
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                insert(name);
            }
        }
    }
}