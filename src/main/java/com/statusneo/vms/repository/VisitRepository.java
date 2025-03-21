package com.statusneo.vms.repository;

import com.statusneo.vms.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    List<Visit> findAllByVisitDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
