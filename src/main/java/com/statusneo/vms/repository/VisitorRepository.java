package com.statusneo.vms.repository;

import com.statusneo.vms.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//@Repository
//public interface VisitorRepository extends JpaRepository<Visitor, Long> {
////	List<Visitor> findAllByVisitDateBetween(LocalDateTime startDate, LocalDateTime endDate);
//	Optional<Visitor> findByPhoneNumber(String phoneNumber);
//}

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
	boolean existsByEmail(String email);  // Add this method

	@Query("SELECT v FROM Visitor v WHERE v.email = :email")
	Optional<Visitor> findByEmail(@Param("email") String email);
}
