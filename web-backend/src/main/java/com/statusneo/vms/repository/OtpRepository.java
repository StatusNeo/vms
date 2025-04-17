
package com.statusneo.vms.repository;

import com.statusneo.vms.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    //    List<Otp> findByEmail(String email);
    @Query("SELECT o FROM Otp o WHERE o.email = :email ORDER BY o.createdAt DESC")
    List<Otp> findByEmailOrdered(@Param("email") String email);
}
