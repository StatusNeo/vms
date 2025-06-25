package com.statusneo.vms.model;

public record AzureADUser(
        String id,
        String displayName,
        String userPrincipalName,
        String mail
) {
}
