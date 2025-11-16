package com.statusneo.vms.cache;

import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;
import com.statusneo.vms.util.TrieNode;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Lightweight in-memory cache of employees (name/email) used by UI host-search.
 * Added search(...) helper so UI queries use the cache rather than hitting DB
 * or external APIs.
 */
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
        if (name == null || name.isBlank())
            return;
        TrieNode node = root;
        for (char c : name.toLowerCase().toCharArray()) {
            node = node.getChildren().computeIfAbsent(c, k -> new TrieNode());
        }
        node.setEndOfWord(true);
        node.addOriginal(name);
    }

    public List<String> getEmployeeNamesByPrefix(String prefix) {
        if (prefix == null)
            prefix = "";
        TrieNode node = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            node = node.getChildren().get(c);
            if (node == null)
                return Collections.emptyList();
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
        if (results.size() >= MAX_SUGGESTIONS)
            return;
        if (node.isEndOfWord()) {
            // add originals for this terminal node
            for (String orig : node.getOriginals()) {
                if (results.size() >= MAX_SUGGESTIONS)
                    break;
                results.add(orig);
            }
        }

        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            if (results.size() >= MAX_SUGGESTIONS)
                break;
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

    // New code starts here
    private final Map<Long, Employee> byId = new ConcurrentHashMap<>();

    /**
     * Search cached employees by name/email/identifier. Case-insensitive substring
     * match.
     * If query is null/empty returns a small default list (first 25).
     */
    public List<Employee> search(String query) {
        Collection<Employee> all = byId.values();
        if (query == null || query.isBlank()) {
            return all.stream()
                    .sorted(Comparator.comparing(Employee::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .limit(25)
                    .collect(Collectors.toList());
        }
        String q = query.toLowerCase(Locale.ROOT).trim();
        return all.stream()
                .filter(e -> {
                    if (e == null)
                        return false;
                    String name = e.getName() == null ? "" : e.getName().toLowerCase(Locale.ROOT);
                    String email = e.getEmail() == null ? "" : e.getEmail().toLowerCase(Locale.ROOT);
                    return name.contains(q) || email.contains(q);
                })
                .sorted(Comparator.comparing(Employee::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .limit(25)
                .collect(Collectors.toList());
    }

    // Optional convenience
    public Optional<Employee> getById(Long id) {
        return Optional.ofNullable(byId.get(id));
    }
    // New code ends here
}