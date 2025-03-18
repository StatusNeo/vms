package com.statusneo.vms.repository;

import com.statusneo.vms.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
//	List<Visitor> findAllByVisitDateBetween(LocalDateTime startDate, LocalDateTime endDate);
	Optional<Visitor> findByPhoneNumber(String phoneNumber);
	List<Visitor> findAll();

}
