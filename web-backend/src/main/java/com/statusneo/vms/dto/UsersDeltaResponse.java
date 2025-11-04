package com.statusneo.vms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UsersDeltaResponse {
    @JsonProperty("@odata.context")
    private String context;

    @JsonProperty("@odata.nextLink")
    private String nextLink;

    @JsonProperty("@odata.deltaLink")
    private String deltaLink; // Optional, appears in final page

    private List<UserResponse> value;

    // Getters and setters

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getNextLink() {
        return nextLink;
    }

    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    public String getDeltaLink() {
        return deltaLink;
    }

    public void setDeltaLink(String deltaLink) {
        this.deltaLink = deltaLink;
    }

    public List<UserResponse> getValue() {
        return value;
    }

    public void setValue(List<UserResponse> value) {
        this.value = value;
    }
}