package com.statusneo.vms.repository;

import com.statusneo.vms.model.VisitingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitingInfoRepository extends JpaRepository<VisitingInfo, Long> {
    List<VisitingInfo> findAllByVisitDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
