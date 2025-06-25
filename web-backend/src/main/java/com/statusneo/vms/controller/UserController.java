package com.statusneo.vms.controller;

import com.statusneo.vms.model.AzureADUser;
import com.statusneo.vms.service.AzureADService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    private final AzureADService azureADService;

    public UserController(AzureADService azureADService) {
        this.azureADService = azureADService;
    }

    @GetMapping
    public List<AzureADUser> getAllUsers() {
        return azureADService.getAllUsers();
    }

    @GetMapping("/{id}")
    public AzureADUser getUserById(@PathVariable String id) {
        return azureADService.getUserById(id);
    }
}
