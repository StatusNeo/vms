package com.statusneo.vms.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.statusneo.vms.cache.EmployeeNameCache;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphDirectoryServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeNameCache employeeNameCache;

    @InjectMocks
    private GraphDirectoryService graphDirectoryService;

    @Test
    void syncAllUsersToEmployees_successfulResponse() {
        Map<String, Object> user = new HashMap<>();
        user.put("displayName", "Alice Smith");
        user.put("mail", "alice@example.com");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("value", List.of(user));

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(employeeRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockRestClientResponse(responseEntity);

        int upserts = graphDirectoryService.syncAllUsersToEmployees();

        assertEquals(1, upserts);
        verify(employeeRepository).save(any(Employee.class));
        verify(employeeNameCache).clear();
        verify(employeeNameCache).bulkInsert(List.of("Alice Smith"));
    }

    @Test
    void syncAllUsersToEmployees_unsuccessfulResponse() {
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>((Map<String, Object>) null, HttpStatus.BAD_REQUEST);

        mockRestClientResponse(responseEntity);

        int upserts = graphDirectoryService.syncAllUsersToEmployees();

        assertEquals(0, upserts);
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(employeeNameCache).clear();
        verify(employeeNameCache).bulkInsert(Collections.emptyList());
    }

    @Test
    void syncAllUsersToEmployees_nullResponseBody() {
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>((Map<String, Object>) null, HttpStatus.OK);

        mockRestClientResponse(responseEntity);

        int upserts = graphDirectoryService.syncAllUsersToEmployees();

        assertEquals(0, upserts);
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(employeeNameCache).clear();
        verify(employeeNameCache).bulkInsert(Collections.emptyList());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void mockRestClientResponse(ResponseEntity<Map<String, Object>> responseEntity) {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(Map.class)).thenReturn((ResponseEntity) responseEntity);
    }
}