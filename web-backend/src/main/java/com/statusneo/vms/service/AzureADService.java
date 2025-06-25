package com.statusneo.vms.service;

import com.microsoft.graph.models.User;
import com.microsoft.graph.models.UserCollectionResponse;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.statusneo.vms.model.AzureADUser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AzureADService {
    private final GraphServiceClient graphClient;

    public AzureADService(GraphServiceClient graphClient) {
        this.graphClient = graphClient;
    }

    @Cacheable(value = "users")
    public List<AzureADUser> getAllUsers() {
        List<AzureADUser> azureADUsers = new ArrayList<>();
        UserCollectionResponse userCollectionResponse = graphClient.users().get(requestConfig -> {
            assert requestConfig.queryParameters != null;
            requestConfig.queryParameters.select = new String[]{"id,displayName,userPrincipalName,mail"};
        });
        while (userCollectionResponse != null) {
            final List<User> users = userCollectionResponse.getValue();
            assert users != null;
            for (User user : users) {
                azureADUsers.add(mapToAzureADUser(user));
            }

            // Get the next page
            final String odataNextLink = userCollectionResponse.getOdataNextLink();
            if (odataNextLink == null || odataNextLink.isEmpty()) {
                break;
            } else {
                userCollectionResponse = graphClient.users().withUrl(odataNextLink).get();
            }
        }
        return azureADUsers;
    }

    private AzureADUser mapToAzureADUser(User graphUser) {
        return new AzureADUser(graphUser.getId(), graphUser.getDisplayName(), graphUser.getUserPrincipalName(), graphUser.getMail());
    }

    @Cacheable(value = "users", key = "#id")
    public AzureADUser getUserById(String id) {
        User graphUser = graphClient.users().byUserId(id).get(requestConfig -> {
            assert requestConfig.queryParameters != null;
            requestConfig.queryParameters.select = new String[]{"id,displayName,userPrincipalName,mail"};
        });
        assert graphUser != null;
        return mapToAzureADUser(graphUser);
    }
}