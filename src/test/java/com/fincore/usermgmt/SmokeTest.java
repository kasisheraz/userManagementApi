package com.fincore.usermgmt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests to verify the application starts correctly and basic endpoints are accessible.
 * These tests ensure the application can boot and respond to basic requests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SmokeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void contextLoads() {
        // Verifies that the Spring context loads successfully
        assertNotNull(restTemplate);
    }

    @Test
    void actuatorHealthEndpoint_ShouldBeAccessible() {
        // Verify health endpoint is up
        String url = getBaseUrl() + "/actuator/health";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("UP"));
    }

    @Test
    void apiAuthLoginEndpoint_ShouldBeAccessible() {
        // Verify login endpoint exists (even if auth fails, it should return 401, not 404)
        String url = getBaseUrl() + "/api/auth/login";
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        
        // Should not be 404 - endpoint exists
        assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        // Expected: 400 (Bad Request) or 401 (Unauthorized) for invalid/empty credentials
        assertTrue(
            response.getStatusCode() == HttpStatus.BAD_REQUEST || 
            response.getStatusCode() == HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    void apiUsersEndpoint_ShouldRequireAuthentication() {
        // Verify users endpoint exists but requires auth
        String url = getBaseUrl() + "/api/users";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Should return 401 or 403 (unauthorized/forbidden) without auth token
        assertTrue(
            response.getStatusCode() == HttpStatus.UNAUTHORIZED || 
            response.getStatusCode() == HttpStatus.FORBIDDEN
        );
    }

    @Test
    void nonExistentEndpoint_ShouldReturn404() {
        // Verify that non-existent endpoints properly return 404
        String url = getBaseUrl() + "/api/nonexistent";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void applicationRuns_WithoutErrors() {
        // This test simply verifies the application started without throwing exceptions
        assertTrue(port > 0, "Application should be running on a valid port");
    }
}
