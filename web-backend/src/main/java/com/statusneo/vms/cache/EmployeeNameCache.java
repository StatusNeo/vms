package com.statusneo.vms.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;
import com.statusneo.vms.util.TrieNode;

import jakarta.annotation.PostConstruct;

@Component
public class EmployeeNameCache {

    private final TrieNode root = new TrieNode();

    @Autowired
    public EmployeeRepository employeeRepository;

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
        TrieNode node = root;
        for (char c : name.toLowerCase().toCharArray()) {
            node = node.getChildren().computeIfAbsent(c, k -> new TrieNode());
        }
        node.setEndOfWord(true);
    }

    public List<String> getEmployeeNamesByPrefix(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            node = node.getChildren().get(c);
            if (node == null) return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();
        collectNames(node, new StringBuilder(prefix.toLowerCase()), results);
        return results;
    }

    private void collectNames(TrieNode node, StringBuilder prefix, List<String> results) {
        if (node.isEndOfWord()) {
            results.add(prefix.toString());
        }

        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            collectNames(entry.getValue(), prefix.append(entry.getKey()), results);
            prefix.setLength(prefix.length() - 1);
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