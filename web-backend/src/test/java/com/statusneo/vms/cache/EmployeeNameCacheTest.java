package com.statusneo.vms.cache;

import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeNameCacheTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeNameCache cache;

    @BeforeEach
    void setup() {
        cache = new EmployeeNameCache(employeeRepository);
    }

    private Employee emp(String name, String email) {
        Employee e = new Employee();
        e.setName(name);
        e.setEmail(email);
        return e;
    }

    @Test
    void initializeCache_populates_from_repository_and_searches_by_prefix() {
        List<Employee> list = Arrays.asList(
                emp("Alice Johnson", "alice@example.com"),
                emp("Bob Smith", "bob@example.com"),
                emp("Alicia Keys", "alicia@example.com")
        );
        when(employeeRepository.findAll()).thenReturn(list);

        cache.initializeCache();

        List<String> aNames = cache.getEmployeeNamesByPrefix("Ali");
        assertTrue(aNames.stream().anyMatch(s -> s.equals("Alice Johnson")));
        assertTrue(aNames.stream().anyMatch(s -> s.equals("Alicia Keys")));

        List<Employee> employees = cache.getEmployeesByPrefix("Bob");
        assertEquals(1, employees.size());
        assertEquals("Bob Smith", employees.get(0).getName());

        Employee byName = cache.getEmployeeByName("Alice Johnson");
        assertNotNull(byName);
        assertEquals("alice@example.com", byName.getEmail());

        Map<String, Object> stats = cache.getCacheStats();
        assertEquals(3, ((Integer) stats.get("totalEmployees")).intValue());
        assertEquals(true, stats.get("cacheInitialized"));
    }

    @Test
    void getEmployeeNamesByPrefix_limits_to_max_suggestions() {
        // create more than MAX_SUGGESTIONS employees with same prefix "Emp"
        List<Employee> many = new ArrayList<>();
        IntStream.range(0, 20).forEach(i -> many.add(emp("EmpUser" + i, "u"+i+"@example.com")));
        when(employeeRepository.findAll()).thenReturn(many);

        cache.initializeCache();

        List<String> results = cache.getEmployeeNamesByPrefix("Emp");
        // MAX_SUGGESTIONS is 10 in implementation
        assertTrue(results.size() <= 10);
        assertEquals(10, results.size());
    }

    @Test
    void clear_removes_all_entries() {
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(emp("One","one@example.com")));
        cache.initializeCache();

        Map<String, Object> statsBefore = cache.getCacheStats();
        assertEquals(1, ((Integer) statsBefore.get("totalEmployees")).intValue());

        cache.clear();
        Map<String, Object> statsAfter = cache.getCacheStats();
        assertEquals(0, ((Integer) statsAfter.get("totalEmployees")).intValue());
        assertEquals(false, statsAfter.get("cacheInitialized"));
    }

    @Test
    void insert_and_update_employee_behaviour() {
        Employee john = emp("John Doe", "john@old.example");
        cache.insertEmployee(john);

        Employee got = cache.getEmployeeByName("John Doe");
        assertNotNull(got);
        assertEquals("john@old.example", got.getEmail());

        // update with new email, same name
        Employee johnUpdated = emp("John Doe", "john@new.example");
        cache.updateEmployee(johnUpdated);

        Employee gotAfter = cache.getEmployeeByName("John Doe");
        assertNotNull(gotAfter);
        assertEquals("john@new.example", gotAfter.getEmail());
    }

    @Test
    void bulkInsertEmployees_adds_many_entries() {
        List<Employee> employees = Arrays.asList(
                emp("A", "a@example.com"),
                emp("B", "b@example.com"),
                emp("C", "c@example.com")
        );
        cache.clear();
        cache.bulkInsertEmployees(employees);

        Map<String, Object> stats = cache.getCacheStats();
        assertEquals(3, ((Integer) stats.get("totalEmployees")).intValue());

        List<Employee> found = cache.getEmployeesByPrefix("");
        // should return up to available employees
        assertTrue(found.size() >= 3);
    }
}