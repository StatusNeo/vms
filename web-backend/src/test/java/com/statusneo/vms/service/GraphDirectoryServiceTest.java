package com.statusneo.vms.service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestTemplate;

import com.statusneo.vms.cache.EmployeeNameCache;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;

class GraphDirectoryServiceTest {

    @Mock
    private OAuth2AuthorizedClientManager authorizedClientManager;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeNameCache employeeNameCache;

    @InjectMocks
    private GraphDirectoryService graphDirectoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private OAuth2AuthorizedClient mockAuthorizedClient(String tokenValue) {
        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                tokenValue,
                Instant.now(),
                Instant.now().plusSeconds(3600));
        ClientRegistration registration = mock(ClientRegistration.class);
        return new OAuth2AuthorizedClient(registration, "principal", token);
    }

    @Test
    void testSyncAllUsersToEmployees_SuccessfulSyncSinglePage() {
        // Mock access token
        when(authorizedClientManager.authorize(any())).thenReturn(mockAuthorizedClient("test-token"));

        // Mock Graph API response
        Map<String, Object> user1 = Map.of(
                "displayName", "John Doe",
                "mail", "john.doe@example.com",
                "userPrincipalName", "john.doe@upn.com");
        Map<String, Object> user2 = Map.of(
                "displayName", "Jane Smith",
                "mail", "",
                "userPrincipalName", "jane.smith@upn.com");
        List<Map<String, Object>> users = List.of(user1, user2);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("value", users);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Mock repository
        when(employeeRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("jane.smith@upn.com")).thenReturn(Optional.empty());

        Employee emp1 = new Employee();
        emp1.setName("John Doe");
        emp1.setEmail("john.doe@example.com");
        Employee emp2 = new Employee();
        emp2.setName("Jane Smith");
        emp2.setEmail("jane.smith@upn.com");

        when(employeeRepository.save(any(Employee.class))).thenReturn(emp1, emp2);

        int upserts = graphDirectoryService.syncAllUsersToEmployees();

        assertEquals(2, upserts);
        verify(employeeNameCache).clear();
        verify(employeeNameCache).bulkInsert(List.of("John Doe", "Jane Smith"));
    }

    @Test
    void testSyncAllUsersToEmployees_SuccessfulSyncWithNextLink() {
        when(authorizedClientManager.authorize(any())).thenReturn(mockAuthorizedClient("test-token"));

        Map<String, Object> user1 = Map.of(
                "displayName", "Alice",
                "mail", "alice@example.com",
                "userPrincipalName", "alice@upn.com");
        List<Map<String, Object>> usersPage1 = List.of(user1);

        Map<String, Object> responseBody1 = new HashMap<>();
        responseBody1.put("value", usersPage1);
        responseBody1.put("@odata.nextLink", "next-page-url");

        ResponseEntity<Map> responseEntity1 = new ResponseEntity<>(responseBody1, HttpStatus.OK);

        Map<String, Object> user2 = Map.of(
                "displayName", "Bob",
                "mail", "bob@example.com",
                "userPrincipalName", "bob@upn.com");
        List<Map<String, Object>> usersPage2 = List.of(user2);

        Map<String, Object> responseBody2 = new HashMap<>();
        responseBody2.put("value", usersPage2);

        ResponseEntity<Map> responseEntity2 = new ResponseEntity<>(responseBody2, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity1)
                .thenReturn(responseEntity2);

        when(employeeRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("bob@example.com")).thenReturn(Optional.empty());

        Employee emp1 = new Employee();
        emp1.setName("Alice");
        emp1.setEmail("alice@example.com");
        Employee emp2 = new Employee();
        emp2.setName("Bob");
        emp2.setEmail("bob@example.com");

        when(employeeRepository.save(any(Employee.class))).thenReturn(emp1, emp2);

        int upserts = graphDirectoryService.syncAllUsersToEmployees();

        assertEquals(2, upserts);
        verify(employeeNameCache).clear();
        verify(employeeNameCache).bulkInsert(List.of("Alice", "Bob"));
    }

    @Test
    void testSyncAllUsersToEmployees_InvalidUserDataSkipped() {
        when(authorizedClientManager.authorize(any())).thenReturn(mockAuthorizedClient("test-token"));

        Map<String, Object> user1 = Map.of(
                "displayName", "",
                "mail", "",
                "userPrincipalName", "");
        List<Map<String, Object>> users = List.of(user1);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("value", users);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        int upserts = graphDirectoryService.syncAllUsersToEmployees();

        assertEquals(0, upserts);
        verify(employeeNameCache).clear();
        verify(employeeNameCache).bulkInsert(Collections.emptyList());
    }

    @Test
    void testSyncAllUsersToEmployees_UnsuccessfulResponse() {
        when(authorizedClientManager.authorize(any())).thenReturn(mockAuthorizedClient("test-token"));

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        int upserts = graphDirectoryService.syncAllUsersToEmployees();

        assertEquals(0, upserts);
        verify(employeeNameCache).clear();
        verify(employeeNameCache).bulkInsert(Collections.emptyList());
    }

    @Test
    void testSyncAllUsersToEmployees_NullResponseBody() {
        when(authorizedClientManager.authorize(any())).thenReturn(mockAuthorizedClient("test-token"));

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        int upserts = graphDirectoryService.syncAllUsersToEmployees();

        assertEquals(0, upserts);
        verify(employeeNameCache).clear();
        verify(employeeNameCache).bulkInsert(Collections.emptyList());
    }

    @Test
    void testSyncAllUsersToEmployees_NullAccessTokenThrowsException() {
        when(authorizedClientManager.authorize(any())).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            graphDirectoryService.syncAllUsersToEmployees();
        });
        assertEquals("Failed to obtain Graph access token", ex.getMessage());
    }

    @Test
    void testSyncAllUsersToEmployees_NullTokenValueThrowsException() {
        OAuth2AuthorizedClient client = Mockito.mock(OAuth2AuthorizedClient.class);
        Mockito.lenient().when(client.getAccessToken()).thenReturn(null);
        lenient().when(client.getAccessToken()).thenReturn(null);
        lenient().when(authorizedClientManager.authorize(any())).thenReturn(client);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            graphDirectoryService.syncAllUsersToEmployees();
        });
        assertEquals("Failed to obtain Graph access token", ex.getMessage());
    }
}