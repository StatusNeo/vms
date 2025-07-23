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
package com.statusneo.vms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

    private String sender;

    private VisitorConfirmation visitorConfirmation = new VisitorConfirmation();
    private HostNotification hostNotification = new HostNotification();

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public VisitorConfirmation getVisitorConfirmation() {
        return visitorConfirmation;
    }

    public void setVisitorConfirmation(VisitorConfirmation visitorConfirmation) {
        this.visitorConfirmation = visitorConfirmation;
    }

    public static class VisitorConfirmation {
        private String subject;
        private String template;

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }
    }

    public HostNotification getHostNotification() {
        return hostNotification;
    }

    public void setHostNotification(HostNotification hostNotification) {
        this.hostNotification = hostNotification;
    }

    public static class HostNotification {
        private String subject;
        private String template;

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }
    }
}