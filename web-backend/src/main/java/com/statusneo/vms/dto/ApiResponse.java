package com.statusneo.vms.dto;

public class ApiResponse {
    private String status;
    private String message;

    // No-arg constructor (required for some serializers/deserializers)
    public ApiResponse() {
    }

    // Parameterized constructor
    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getter for status
    public String getStatus() {
        return status;
    }

    // Setter for status
    public void setStatus(String status) {
        this.status = status;
    }

    // Getter for message
    public String getMessage() {
        return message;
    }

    // Setter for message
    public void setMessage(String message) {
        this.message = message;
    }
}
