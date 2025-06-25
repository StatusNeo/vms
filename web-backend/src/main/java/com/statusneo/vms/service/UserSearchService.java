package com.statusneo.vms.service;

import com.statusneo.vms.config.UserTrie;
import com.statusneo.vms.model.AzureADUser;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserSearchService {
    private final AzureADService azureADService;

    private final UserTrie userTrie;

    public UserSearchService(AzureADService azureADService) {
        this.userTrie = new UserTrie();
        this.azureADService = azureADService;
    }

    @PostConstruct
    @Scheduled(fixedRateString = "${azure.user.refresh-rate:3600000}") // Default 1 hour
    public void refreshUserIndex() {
        azureADService.getAllUsers().forEach(userTrie::addUser);
    }

    public Set<AzureADUser> searchUsers(String prefix, int limit) {
        return userTrie.search(prefix, limit);
    }
}
