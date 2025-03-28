package com.statusneo.vms.repository;

import com.statusneo.vms.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e WHERE LOWER(e.name) LIKE LOWER(CONCAT(:prefix, '%'))")
    List<Employee> findByNameStartingWith(@Param("prefix") String prefix);
}