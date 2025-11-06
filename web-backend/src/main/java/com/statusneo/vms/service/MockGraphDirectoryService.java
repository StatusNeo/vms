package com.statusneo.vms.service;

import com.statusneo.vms.dto.DirectorySyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("sqlite")
@Service
public class MockGraphDirectoryService extends GraphDirectoryService {

    private static final Logger logger = LoggerFactory.getLogger(MockGraphDirectoryService.class);

    public MockGraphDirectoryService() {
        super(null, null, null, null); // Pass nulls since we won't use these in mock
    }

    @Override
    public DirectorySyncResult syncAllUsersToEmployees() {
        logger.info("MockGraphDirectoryService: Skipping actual Graph sync, returning dummy result.");
        return new DirectorySyncResult(0, null); // No users synced, no delta link
    }

    public String getAccessToken() {
        logger.info("MockGraphDirectoryService: Returning mock access token.");
        return "mock-access-token";
    }
}
