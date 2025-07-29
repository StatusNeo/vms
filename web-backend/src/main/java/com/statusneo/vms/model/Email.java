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

import java.util.List;
import java.util.Objects;

/**
 * Represents an email message to be sent.
 *
 * @param from        The sender's email address.
 * @param to          The list of recipient email addresses.
 * @param subject     The subject of the email.
 * @param body        The body content of the email.
 * @param attachments The list of attachments (optional, may be null or empty).
 */
public record Email(
    String from,
    List<String> to,
    String subject,
    String body,
    List<Attachment> attachments
) {
    /**
     * Creates an Email for a single recipient, without attachments.
     */
    public static Email of(String from, String to, String subject, String body) {
        return new Email(from, List.of(to), subject, body, List.of());
    }
    /**
     * Creates an Email for multiple recipients, without attachments.
     */
    public static Email of(String from, List<String> to, String subject, String body) {
        return new Email(from, to, subject, body, List.of());
    }
    public Email {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        Objects.requireNonNull(subject, "subject must not be null");
        Objects.requireNonNull(body, "body must not be null");
        // attachments can be null (optional)
    }
} 