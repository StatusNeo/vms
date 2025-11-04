package com.statusneo.vms.dto;

/**
 * Result container for directory synchronization operations.
 * Contains the number of upserts performed and the delta link for future incremental syncs.
 *
 * @param upserts the total number of employee records created or updated
 * @param deltaLink the delta link from Microsoft Graph API for future incremental syncs (null if not available)
 */
public record DirectorySyncResult(int upserts, String deltaLink) {
}

