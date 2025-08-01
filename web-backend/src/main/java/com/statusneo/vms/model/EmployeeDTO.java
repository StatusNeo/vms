package com.statusneo.vms.model;

public class EmployeeDTO {

    private String displayName;
    private String mail;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public EmployeeDTO(String displayName, String mail) {
        this.displayName = displayName;
        this.mail = mail;
    }
}
