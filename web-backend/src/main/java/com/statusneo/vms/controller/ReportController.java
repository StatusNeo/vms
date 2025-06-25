package com.statusneo.vms.controller;

import com.statusneo.vms.model.Visit;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for generating visitor reports.
 *
 * @deprecated This controller is scheduled for removal as it is not needed.
 */
@Deprecated(since = "1.0", forRemoval = true)
@Controller
class ReportController {

    @GetMapping("/report")
    public ResponseEntity<?> getReport(@RequestParam String period) {
        List<Visit> visit;
        if (period.equals("daily")) {
//            visit = visitRepository.findAllByVisitDateBetween(LocalDateTime.now().toLocalDate().atStartOfDay(), LocalDateTime.now());
        } else if (period.equals("monthly")) {
//            visit = visitRepository.findAllByVisitDateBetween(LocalDateTime.now().minusMonths(1), LocalDateTime.now());
        } else {
            return ResponseEntity.badRequest().body("Invalid period");
        }
//        return ResponseEntity.ok(visit);
        return ResponseEntity.ok().build();
    }
}
