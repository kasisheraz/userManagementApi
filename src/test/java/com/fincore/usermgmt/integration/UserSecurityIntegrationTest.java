package com.fincore.usermgmt.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for User Security Features
 * 
 * Tests that protected roles (ADMIN, SUPER_ADMIN, SYSTEM_ADMINISTRATOR) cannot be:
 * - Created via API
 * - Modified via API
 * - Deleted via API
 * - Returned in user list
 * 
 * Run with: mvn test -Dtest=UserSecurityIntegrationTest
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserSecurityIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private String jwtToken;
    private Long testUserId;
    
    @BeforeAll
    void setup() throws Exception {
        baseUrl = "http://localhost:" + port + "/api/users";
        
        // Authenticate and get JWT token
        authenticateAndGetToken();
    }
    
    /**
     * Authenticate with the API and obtain JWT token for subsequent requests
     */
    private void authenticateAndGetToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Step 1: Request OTP
        String otpRequest = "{\"phoneNumber\":\"+1234567890\"}";
        HttpEntity<String> request = new HttpEntity<>(otpRequest, headers);
        
        ResponseEntity<String> otpResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/request-otp",
                request,
                String.class
        );
        
        assertThat(otpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode otpJson = objectMapper.readTree(otpResponse.getBody());
        
        // Get OTP - in test env there should be devOtp
        String otp;
        if (otpJson.has("devOtp")) {
            otp = otpJson.get("devOtp").asText();
        } else {
            // Fallback: try using a default test OTP if devOtp not available
            System.out.println("devOtp not found in response. Response: " + otpResponse.getBody());
            otp = "123456"; // Default test OTP
        }
        
        // Step 2: Verify OTP and get token
        String verifyRequest = String.format("{\"phoneNumber\":\"+1234567890\",\"otp\":\"%s\"}", otp);
        HttpEntity<String> verifyEntity = new HttpEntity<>(verifyRequest, headers);
        
        ResponseEntity<String> verifyResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/verify-otp",
                verifyEntity,
                String.class
        );
        
        assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode verifyJson = objectMapper.readTree(verifyResponse.getBody());
        jwtToken = verifyJson.has("token") 
                ? verifyJson.get("token").asText() 
                : verifyJson.get("accessToken").asText();
                
        assertThat(jwtToken).isNotEmpty();
    }
    
    /**
     * Create HTTP headers with JWT authentication
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken);
        return headers;
    }

    // ============================================
    // CREATE USER TESTS - Role Security
    // ============================================

    @Test
    @Order(1)
    @DisplayName("Create User - Should allow USER role")
    void testCreateUserWithUserRole() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        
        String requestBody = "{\n" +
                "  \"email\": \"test.user@fincore.com\",\n" +
                "  \"phoneNumber\": \"+1111111111\",\n" +
                "  \"firstName\": \"Test\",\n" +
                "  \"lastName\": \"User\",\n" +
                "  \"role\": \"USER\"\n" +
                "}";
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertThat(jsonResponse.get("role").asText()).isEqualTo("USER");
        assertThat(jsonResponse.get("email").asText()).isEqualTo("test.user@fincore.com");
        
        testUserId = jsonResponse.get("id").asLong();
    }

    @Test
    @Order(2)
    @DisplayName("Create User - Should allow MANAGER role")
    void testCreateUserWithManagerRole() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        
        String requestBody = "{\n" +
                "  \"email\": \"test.manager@fincore.com\",\n" +
                "  \"phoneNumber\": \"+2222222222\",\n" +
                "  \"firstName\": \"Test\",\n" +
                "  \"lastName\": \"Manager\",\n" +
                "  \"role\": \"MANAGER\"\n" +
                "}";
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertThat(jsonResponse.get("role").asText()).isEqualTo("MANAGER");
    }

    @Test
    @Order(3)
    @DisplayName("Create User - Should reject ADMIN role and default to USER")
    void testCreateUserWithAdminRole() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        
        String requestBody = "{\n" +
                "  \"email\": \"attempt.admin@fincore.com\",\n" +
                "  \"phoneNumber\": \"+3333333333\",\n" +
                "  \"firstName\": \"Attempt\",\n" +
                "  \"lastName\": \"Admin\",\n" +
                "  \"role\": \"ADMIN\"\n" +
                "}";
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl,
                request,
                String.class
        );

        // Should succeed but with USER role instead of ADMIN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertThat(jsonResponse.get("role").asText()).isEqualTo("USER");
        assertThat(jsonResponse.get("role").asText()).isNotEqualTo("ADMIN");
    }

    @Test
    @Order(4)
    @DisplayName("Create User - Should reject SUPER_ADMIN role and default to USER")
    void testCreateUserWithSuperAdminRole() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        
        String requestBody = "{\n" +
                "  \"email\": \"attempt.superadmin@fincore.com\",\n" +
                "  \"phoneNumber\": \"+4444444444\",\n" +
                "  \"firstName\": \"Attempt\",\n" +
                "  \"lastName\": \"SuperAdmin\",\n" +
                "  \"role\": \"SUPER_ADMIN\"\n" +
                "}";
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl,
                request,
                String.class
        );

        // Should succeed but with USER role instead of SUPER_ADMIN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertThat(jsonResponse.get("role").asText()).isEqualTo("USER");
        assertThat(jsonResponse.get("role").asText()).isNotEqualTo("SUPER_ADMIN");
    }

    @Test
    @Order(5)
    @DisplayName("Create User - Should reject SYSTEM_ADMINISTRATOR role and default to USER")
    void testCreateUserWithSystemAdministratorRole() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        
        String requestBody = "{\n" +
                "  \"email\": \"attempt.sysadmin@fincore.com\",\n" +
                "  \"phoneNumber\": \"+5555555555\",\n" +
                "  \"firstName\": \"Attempt\",\n" +
                "  \"lastName\": \"SysAdmin\",\n" +
                "  \"role\": \"SYSTEM_ADMINISTRATOR\"\n" +
                "}";
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl,
                request,
                String.class
        );

        // Should succeed but with USER role instead of SYSTEM_ADMINISTRATOR
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertThat(jsonResponse.get("role").asText()).isEqualTo("USER");
        assertThat(jsonResponse.get("role").asText()).isNotEqualTo("SYSTEM_ADMINISTRATOR");
    }

    // ============================================
    // UPDATE USER TESTS - Protected Role Security
    // ============================================

    @Test
    @Order(6)
    @DisplayName("Update User - Should allow updating USER role")
    void testUpdateUserWithUserRole() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        
        String requestBody = "{\n" +
                "  \"firstName\": \"Updated\",\n" +
                "  \"lastName\": \"User\"\n" +
                "}";
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + testUserId,
                HttpMethod.PUT,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertThat(jsonResponse.get("firstName").asText()).isEqualTo("Updated");
    }

    @Test
    @Order(7)
    @DisplayName("Update User - Should prevent updating SYSTEM_ADMINISTRATOR (admin user)")
    void testUpdateProtectedAdminUser() throws Exception {
        // Assuming admin user exists with ID 1 and SYSTEM_ADMINISTRATOR role
        HttpHeaders headers = createAuthHeaders();
        
        String requestBody = "{\n" +
                "  \"firstName\": \"Hacked\",\n" +
                "  \"lastName\": \"Admin\"\n" +
                "}";
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/1",
                HttpMethod.PUT,
                request,
                String.class
        );

        // Should return 403 Forbidden
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertThat(jsonResponse.get("message").asText()).contains("Cannot modify users with protected roles");
    }

    // ============================================
    // DELETE USER TESTS - Protected Role Security
    // ============================================

    @Test
    @Order(8)
    @DisplayName("Delete User - Should allow deleting USER role")
    void testDeleteUserWithUserRole() {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + testUserId,
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(9)
    @DisplayName("Delete User - Should prevent deleting SYSTEM_ADMINISTRATOR (admin user)")
    void testDeleteProtectedAdminUser() {
        // Attempting to delete admin user (ID 1)
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/1",
                HttpMethod.DELETE,
                entity,
                String.class
        );

        // Should return 403 Forbidden
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        
        try {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            assertThat(jsonResponse.get("message").asText()).contains("Cannot delete users with protected roles");
        } catch (Exception e) {
            // If parsing fails, at least verify 403
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    // ============================================
    // GET USERS TESTS - Admin Filtering
    // ============================================

    @Test
    @Order(10)
    @DisplayName("Get All Users - Should filter out protected role users (ADMIN, SUPER_ADMIN, SYSTEM_ADMINISTRATOR)")
    void testGetAllUsersFiltersProtectedRoles() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertThat(jsonResponse.isArray()).isTrue();
        
        // Verify no user has protected roles in the response
        for (JsonNode user : jsonResponse) {
            String role = user.get("role").asText();
            assertThat(role).isNotIn("ADMIN", "SUPER_ADMIN", "SYSTEM_ADMINISTRATOR");
        }
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Test
    @Order(11)
    @DisplayName("Create User - Should handle null role by defaulting to USER")
    void testCreateUserWithNullRole() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        
        String requestBody = "{\n" +
                "  \"email\": \"nullrole@fincore.com\",\n" +
                "  \"phoneNumber\": \"+6666666666\",\n" +
                "  \"firstName\": \"Null\",\n" +
                "  \"lastName\": \"Role\"\n" +
                "}";
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertThat(jsonResponse.get("role").asText()).isEqualTo("USER");
    }

    @Test
    @Order(12)
    @DisplayName("Create User - Should handle empty role by defaulting to USER")
    void testCreateUserWithEmptyRole() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        
        String requestBody = "{\n" +
                "  \"email\": \"emptyrole@fincore.com\",\n" +
                "  \"phoneNumber\": \"+7777777777\",\n" +
                "  \"firstName\": \"Empty\",\n" +
                "  \"lastName\": \"Role\",\n" +
                "  \"role\": \"\"\n" +
                "}";
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertThat(jsonResponse.get("role").asText()).isEqualTo("USER");
    }

    @Test
    @Order(13)
    @DisplayName("Create User - Should handle invalid/unknown role by defaulting to USER")
    void testCreateUserWithInvalidRole() throws Exception {
        HttpHeaders headers = createAuthHeaders();
        
        String requestBody = "{\n" +
                "  \"email\": \"invalidrole@fincore.com\",\n" +
                "  \"phoneNumber\": \"+8888888888\",\n" +
                "  \"firstName\": \"Invalid\",\n" +
                "  \"lastName\": \"Role\",\n" +
                "  \"role\": \"HACKER\"\n" +
                "}";
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertThat(jsonResponse.get("role").asText()).isEqualTo("USER");
        assertThat(jsonResponse.get("role").asText()).isNotEqualTo("HACKER");
    }
}
