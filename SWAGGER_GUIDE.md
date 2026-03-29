# Swagger/OpenAPI Documentation Guide

## Overview
This project now includes comprehensive Swagger/OpenAPI 3.0 documentation for all API endpoints. This provides an interactive API documentation interface that allows developers to explore, test, and understand the API without writing code.

## Accessing Swagger UI

### Local Development
```
http://localhost:8080/swagger-ui.html
```

### NPE Environment
```
https://fincore-npe-api-994490239798.europe-west2.run.app/swagger-ui.html
```

### Production Environment
```
https://fincore-prod-api.example.com/swagger-ui.html
```

## Features

### 1. Interactive API Explorer
- **Browse all endpoints** - Organized by controller tags
- **View request/response schemas** - Complete DTO documentation
- **Try it out** - Test APIs directly from the browser
- **See examples** - Sample requests and responses
- **Authentication** - Built-in JWT token management

### 2. API Documentation
- **Complete endpoint descriptions** - What each API does
- **Parameter documentation** - All path, query, and body parameters
- **Response codes** - All possible HTTP status codes
- **Error responses** - Expected error formats
- **Security requirements** - Which endpoints need authentication

### 3. Schema Browser
- **DTO definitions** - All request and response objects
- **Field descriptions** - Detailed property information
- **Validation rules** - Required fields, formats, constraints
- **Nested objects** - Complex object relationships

## Using Swagger UI

### Step 1: Open Swagger UI
Navigate to the Swagger UI URL in your browser:
```
http://localhost:8080/swagger-ui.html
```

### Step 2: Authenticate (for Protected Endpoints)

1. **Get OTP**
   - Expand "Authentication" section
   - Click on `POST /api/auth/request-otp`
   - Click "Try it out"
   - Enter request body:
     ```json
     {
       "phoneNumber": "+1234567890"
     }
     ```
   - Click "Execute"
   - Note the OTP from response (in development, it's logged)

2. **Verify OTP and Get Token**
   - Click on `POST /api/auth/verify-otp`
   - Click "Try it out"
   - Enter request body:
     ```json
     {
       "phoneNumber": "+1234567890",
       "otp": "123456"
     }
     ```
   - Click "Execute"
   - Copy the `accessToken` from the response

3. **Set Authorization Header**
   - Click the "Authorize" button at the top right
   - In the "bearerAuth" dialog, enter: `Bearer <your_access_token>`
   - Click "Authorize"
   - Click "Close"

✅ You're now authenticated! All protected endpoints will use this token.

### Step 3: Test an Endpoint

Example - Get All Users:

1. Expand "User Management" section
2. Click on `GET /api/users`
3. Click "Try it out"
4. Click "Execute"
5. View the response:
   - **Response body** - JSON data returned
   - **Response headers** - HTTP headers
   - **Status code** - 200 OK
   - **cURL command** - Copy/paste into terminal

### Step 4: Create a Resource

Example - Create a User:

1. Expand "User Management" section
2. Click on `POST /api/users`
3. Click "Try it out"
4. Edit the request body:
   ```json
   {
     "phoneNumber": "+447700900000",
     "email": "john.doe@example.com",
     "firstName": "John",
     "lastName": "Doe",
     "dateOfBirth": "1990-05-15",
     "role": "OPERATIONAL_STAFF"
   }
   ```
5. Click "Execute"
6. View the response - should return 201 Created

## API Sections

### 1. Authentication
**Tag:** Authentication  
**Endpoints:**
- `POST /api/auth/request-otp` - Send OTP to phone number
- `POST /api/auth/verify-otp` - Verify OTP and get JWT token
- `GET /api/auth/me` - Get current authenticated user

**Authentication:** Public (request/verify), Protected (me)

### 2. User Management
**Tag:** User Management  
**Endpoints:**
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

**Authentication:** Protected (requires JWT)

### 3. Address Management
**Tag:** Address Management  
**Endpoints:**
- `GET /api/addresses` - List all addresses
- `GET /api/addresses/{id}` - Get address by ID
- `POST /api/addresses` - Create new address
- `PUT /api/addresses/{id}` - Update address
- `DELETE /api/addresses/{id}` - Delete address
- `GET /api/addresses/user/{userId}` - Get addresses by user

**Authentication:** Protected

### 4. Organisation Management
**Tag:** Organisation Management  
**Endpoints:**
- `GET /api/organisations` - List organisations
- `GET /api/organisations/{id}` - Get organisation by ID
- `POST /api/organisations` - Create organisation
- `PUT /api/organisations/{id}` - Update organisation
- `DELETE /api/organisations/{id}` - Delete organisation
- `GET /api/organisations/search` - Search organisations
- `GET /api/organisations/user/{userId}` - Get organisations by user

**Authentication:** Protected

### 5. KYC Document Management
**Tag:** KYC Document Management  
**Endpoints:**
- `GET /api/kyc-documents` - List all documents
- `GET /api/kyc-documents/{id}` - Get document by ID
- `POST /api/kyc-documents` - Upload new document
- `PUT /api/kyc-documents/{id}` - Update document
- `DELETE /api/kyc-documents/{id}` - Delete document
- `GET /api/kyc-documents/organisation/{orgId}` - Get documents by organisation
- `GET /api/kyc-documents/organisation/{orgId}/type/{type}` - Get documents by type

**Authentication:** Protected

### 6. KYC Verification
**Tag:** KYC Verification  
**Endpoints:**
- `GET /api/kyc-verification` - List verifications
- `GET /api/kyc-verification/{id}` - Get verification by ID
- `POST /api/kyc-verification` - Create verification
- `PUT /api/kyc-verification/{id}` - Update verification
- `GET /api/kyc-verification/user/{userId}` - Get user verifications
- `POST /api/kyc-verification/{id}/approve` - Approve verification
- `POST /api/kyc-verification/{id}/reject` - Reject verification

**Authentication:** Protected

### 7. Questionnaire Management
**Tag:** Questionnaire Management  
**Endpoints:**
- `GET /api/questionnaires` - List questionnaires
- `GET /api/questionnaires/{id}` - Get questionnaire by ID
- `POST /api/questionnaires` - Create questionnaire
- `PUT /api/questionnaires/{id}` - Update questionnaire
- `DELETE /api/questionnaires/{id}` - Delete questionnaire
- `GET /api/questionnaires/{id}/questions` - Get questions

**Authentication:** Protected

### 8. Questions
**Tag:** Question Management  
**Endpoints:**
- `GET /api/questions` - List all questions
- `GET /api/questions/{id}` - Get question by ID
- `POST /api/questions` - Create question

**Authentication:** Protected

### 9. Customer Answers
**Tag:** Customer Answer Management  
**Endpoints:**
- `GET /api/customer-answers` - List all answers
- `GET /api/customer-answers/{id}` - Get answer by ID
- `POST /api/customer-answers` - Submit answer
- `PUT /api/customer-answers/{id}` - Update answer
- `DELETE /api/customer-answers/{id}` - Delete answer
- `GET /api/customer-answers/customer/{customerId}` - Get customer answers
- `POST /api/customer-answers/bulk` - Submit bulk answers

**Authentication:** Protected

### 10. System Information
**Tag:** System Information  
**Endpoints:**
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info
- `GET /api/system/version` - Get API version

**Authentication:** Public

## OpenAPI Specification

### JSON Format
Access the raw OpenAPI specification:
```
http://localhost:8080/api-docs
```

### YAML Format
Download OpenAPI spec in YAML:
```
http://localhost:8080/api-docs.yaml
```

### Use Cases
- **Code Generation** - Generate client SDKs
- **API Testing** - Import into Postman/Insomnia
- **Documentation** - Generate static docs
- **Contract Testing** - Validate API responses

## Generating Client Code

### Using OpenAPI Generator

1. **Install OpenAPI Generator**
   ```bash
   npm install @openapitools/openapi-generator-cli -g
   ```

2. **Generate Client**
   ```bash
   # JavaScript/TypeScript
   openapi-generator-cli generate \
     -i http://localhost:8080/api-docs \
     -g typescript-axios \
     -o ./generated-client
   
   # Python
   openapi-generator-cli generate \
     -i http://localhost:8080/api-docs \
     -g python \
     -o ./python-client
   
   # Java
   openapi-generator-cli generate \
     -i http://localhost:8080/api-docs \
     -g java \
     -o ./java-client
   ```

3. **Use Generated Client**
   ```javascript
   // TypeScript example
   import { UserManagementApi } from './generated-client';
   
   const api = new UserManagementApi();
   const users = await api.getAllUsers();
   ```

## Import into Postman

### Step 1: Export OpenAPI Spec
1. Navigate to `http://localhost:8080/api-docs`
2. Save the JSON response to a file: `openapi.json`

### Step 2: Import to Postman
1. Open Postman
2. Click "Import" button
3. Select "File" tab
4. Choose `openapi.json`
5. Click "Import"

### Result
- ✅ All endpoints imported
- ✅ Request/response schemas included
- ✅ Authentication configured
- ✅ Examples available

## Configuration

### Application Properties

The OpenAPI configuration is defined in `OpenAPIConfig.java`:

```java
@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("FinCore User Management API")
                .description("Complete API for User Management and Organisation Onboarding")
                .version("1.0.0"))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local"),
                new Server().url("https://fincore-npe-api...").description("NPE")
            ))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            );
    }
}
```

### SpringDoc Properties

In `application.yml`:

```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
    tagsSorter: alpha
```

## Best Practices

### For API Developers

1. **Document Everything**
   ```java
   @Operation(
       summary = "Create user",
       description = "Creates a new user with the provided information"
   )
   ```

2. **Include Examples**
   ```java
   @Parameter(
       description = "User ID",
       required = true,
       example = "1"
   )
   ```

3. **Document All Responses**
   ```java
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Success"),
       @ApiResponse(responseCode = "400", description = "Bad Request"),
       @ApiResponse(responseCode = "401", description = "Unauthorized")
   })
   ```

4. **Use Schema Annotations**
   ```java
   @Schema(description = "User data transfer object", example = "...")
   public class UserDTO {
       @Schema(description = "Unique user identifier", example = "1")
       private Long id;
   }
   ```

### For API Consumers

1. **Start with Swagger UI** - Explore APIs interactively
2. **Test Before Coding** - Verify responses in Swagger
3. **Check Examples** - Use provided examples as templates
4. **Review Schemas** - Understand object structures
5. **Note Error Responses** - Plan error handling

## Troubleshooting

### Swagger UI Not Loading

**Problem:** Blank page or 404 error

**Solutions:**
1. Verify application is running: `curl http://localhost:8080/actuator/health`
2. Check SpringDoc dependency in `pom.xml`
3. Verify configuration in `application.yml`
4. Clear browser cache

### Missing Endpoints

**Problem:** Some endpoints don't appear in Swagger UI

**Solutions:**
1. Verify controller has `@RestController` annotation
2. Check if controller is component-scanned
3. Ensure methods have `@GetMapping`, `@PostMapping`, etc.
4. Restart application

### Authentication Not Working

**Problem:** "Unauthorized" errors after setting token

**Solutions:**
1. Verify token format: `Bearer <token>` (note the space)
2. Check token hasn't expired
3. Ensure token was obtained from `/api/auth/verify-otp`
4. Click "Authorize" button and set token correctly

### Schema Not Showing

**Problem:** Request/response schemas are empty

**Solutions:**
1. Add `@Schema` annotations to DTOs
2. Verify DTOs are in scanned packages
3. Check for circular references in DTOs
4. Restart application

## Examples

### Complete User Creation Workflow

```bash
# 1. Request OTP
curl -X POST http://localhost:8080/api/auth/request-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+1234567890"}'

# 2. Verify OTP (check logs for OTP code)
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1234567890",
    "otp": "123456"
  }'
# Save the accessToken from response

# 3. Create User
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "phoneNumber": "+447700900000",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-05-15",
    "role": "OPERATIONAL_STAFF"
  }'

# 4. List Users
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Integration with Frontend

### React Example

```typescript
// api/client.ts
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// api/users.ts
export const userApi = {
  getAll: () => apiClient.get('/api/users'),
  getById: (id: number) => apiClient.get(`/api/users/${id}`),
  create: (data: UserCreateDTO) => apiClient.post('/api/users', data),
  update: (id: number, data: UserUpdateDTO) => apiClient.put(`/api/users/${id}`, data),
  delete: (id: number) => apiClient.delete(`/api/users/${id}`),
};
```

### Angular Example

```typescript
// services/user.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('accessToken');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  getAllUsers(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(this.apiUrl, { 
      headers: this.getHeaders() 
    });
  }

  createUser(user: UserCreateDTO): Observable<UserDTO> {
    return this.http.post<UserDTO>(this.apiUrl, user, { 
      headers: this.getHeaders() 
    });
  }
}
```

## Support

For issues or questions:
- **Swagger UI Issues**: Check browser console for errors
- **Missing Documentation**: Verify controller annotations
- **Authentication Problems**: Review JWT token format
- **Schema Issues**: Check DTO annotations

## Summary

**With Swagger/OpenAPI:**
- ✅ Complete interactive API documentation
- ✅ Test APIs without writing code
- ✅ Generate client SDKs automatically
- ✅ Import into Postman easily
- ✅ Better developer experience
- ✅ Reduced integration time

**Access Points:**
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **OpenAPI YAML**: http://localhost:8080/api-docs.yaml

Happy API exploring! 🚀
