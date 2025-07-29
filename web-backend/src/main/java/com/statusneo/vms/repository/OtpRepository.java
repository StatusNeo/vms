/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.statusneo.vms.repository;

import com.statusneo.vms.model.Otp;
import com.statusneo.vms.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    @Query("SELECT COUNT(o) > 0 FROM Otp o WHERE o.visit = :visit AND o.otp = :otpCode AND o.expirationTime > :currentTime ORDER BY o.createdAt DESC LIMIT 1")
    boolean isValidOtp(@Param("visit") Visit visit, @Param("otpCode") String otpCode, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT o FROM Otp o WHERE o.visit.id = :visitId ORDER BY o.createdAt DESC LIMIT 1")
    Optional<Otp> findFirstByVisitIdOrderByCreatedAtDesc(@Param("visitId") Long visitId);
    Optional<Otp> findFirstByVisitOrderByCreatedAtDesc(Visit visit);
}
