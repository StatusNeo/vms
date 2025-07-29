/*
 * Copyright [2025] StatusNeo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: StatusNeo
=======
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
package com.statusneo.vms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a visit record in the Visitor Management System.
 * This entity tracks visitor check-ins, approvals, and visit details.
 * Each visit can have multiple OTPs associated with it for visitor email validation.
 */
@Entity
@Table(name = "visit")
public class Visit {
    /**
     * Unique identifier for the visit record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The visitor associated with this visit.
     */

    @ManyToOne
    @JoinColumn(name = "visitor_id")
    private Visitor visitor;

    /**
     * The host employee that the visitor is meeting.
     */
    private String host;
    private String otp;
    private Boolean isApproved = false;

    /**
     * Date and time of the visit.
     */
    @Column(name = "visit_date")
    private LocalDateTime visitDate;

    /**
     * Default constructor required by JPA.
     */
    public Visit() {
    }

    /**
     * Constructs a new Visit with the specified details.
     *
     * @param id The unique identifier
     * @param visitor The visitor associated with this visit
     * @param host The host employee
     * @param isApproved The approval status
     * @param visitDate The date and time of the visit
     */
    public Visit(Long id, Visitor visitor, String host, Boolean isApproved, LocalDateTime visitDate) {
        this.id = id;
        this.visitor = visitor;
        this.host = host;
        this.otp = otp;
        this.isApproved = isApproved;
        this.visitDate = visitDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Visitor getVisitor() {
        return visitor;
    }

    public void setVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public LocalDateTime getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDateTime visitDate) {
        this.visitDate = visitDate;
    }

    @Override
    public String toString() {
        return "VisitingInfo [id=" + id + ", visitor=" + visitor + ", host=" + host + ", otp=" + otp +
                ", isApproved=" + isApproved + ", visitDate=" + visitDate + "]";
    }
}