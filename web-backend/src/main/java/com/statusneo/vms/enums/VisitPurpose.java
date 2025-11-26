package com.statusneo.vms.enums;

public enum VisitPurpose {
    VENDOR("Vendor"),
    CLIENT_MEETING("Client Meeting"),
    INTERVIEW("Interview"),
    BUSINESS_MEETING("Business Meeting"),
    DELIVERY("Delivery");

    private final String displayName;

    VisitPurpose(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFormValue() {
        return this.name().toLowerCase().replace('_', '-');
    }
}
