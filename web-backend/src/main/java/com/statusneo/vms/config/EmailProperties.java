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