package com.statusneo.vms.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;
import com.statusneo.vms.util.TrieNode;

import jakarta.annotation.PostConstruct;

@Component
public class EmployeeNameCache {

    private static final int MAX_SUGGESTIONS = 10;

    private final TrieNode root = new TrieNode();
    private final Map<String, Employee> nameToEmployeeMap = new HashMap<>();
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
                nameToEmployeeMap.put(employee.getName(), employee);
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

    public void insertEmployee(Employee employee) {
        if (employee == null || employee.getName() == null || employee.getName().isBlank()) return;
        insert(employee.getName());
        nameToEmployeeMap.put(employee.getName(), employee);
    }

    public List<String> getEmployeeNamesByPrefix(String prefix) {
        if (prefix == null) prefix = "";
        TrieNode node = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            node = node.getChildren().get(c);
            if (node == null) return Collections.emptyList();
        }

        Set<String> results = new LinkedHashSet<>();
        collectNames(node, results);

        List<String> list = new ArrayList<>(results);
        if (list.size() > MAX_SUGGESTIONS) {
            return list.subList(0, MAX_SUGGESTIONS);
        }
        return list;
    }

    public List<Employee> getEmployeesByPrefix(String prefix) {
        List<String> names = getEmployeeNamesByPrefix(prefix);
        return names.stream()
                .map(name -> nameToEmployeeMap.get(name))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Employee getEmployeeByName(String name) {
        return nameToEmployeeMap.get(name);
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
        nameToEmployeeMap.clear();
    }

    public void bulkInsert(Collection<String> names) {
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                insert(name);
            }
        }
    }
    public void bulkInsertEmployees(Collection<Employee> employees) {
        for (Employee employee : employees) {
            if (employee != null && employee.getName() != null && !employee.getName().isBlank()) {
                insertEmployee(employee);
            }
        }
    }

    public void updateEmployee(Employee employee) {
        if (employee == null || employee.getName() == null || employee.getName().isBlank()) return;

        nameToEmployeeMap.put(employee.getName(), employee);
    }

    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", nameToEmployeeMap.size());
        stats.put("cacheInitialized", !nameToEmployeeMap.isEmpty());
        return stats;
    }
}