package com.statusneo.vms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.statusneo.vms.model.Visitor;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
//	List<Visitor> findAllByVisitDateBetween(LocalDateTime startDate, LocalDateTime endDate);
	Optional<Visitor> findByPhoneNumber(String phoneNumber);
}
