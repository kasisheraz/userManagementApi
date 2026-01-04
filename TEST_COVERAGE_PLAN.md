# Test Coverage Analysis and Improvement Plan

## Current Test Coverage Summary

**Overall Coverage: 22% of instructions**

### Coverage by Package:

| Package | Instruction Coverage | Branch Coverage | Key Observations |
|---------|---------------------|-----------------|------------------|
| **com.fincore.usermgmt.config** | 72% | 8% | ✅ Good coverage |
| **com.fincore.usermgmt.service** | 64% | 30% | ✅ Good coverage |
| **com.fincore.usermgmt.controller** | 51% | 3% | ⚠️ Needs branch testing |
| **com.fincore.usermgmt.dto** | 18% | 0% | ❌ Low coverage |
| **com.fincore.usermgmt.entity** | 18% | 0% | ❌ Low coverage |
| **com.fincore.usermgmt.security** | 17% | 11% | ❌ Low coverage |
| **com.fincore.usermgmt.mapper** | 2% | 0% | ❌ Very low coverage |
| **com.fincore.usermgmt** | 13% | n/a | Main application class |

### Test Files Currently Available:
1. ✅ `OrganisationServiceTest.java`
2. ✅ `UserServiceTest.java`
3. ✅ `KycDocumentServiceTest.java`
4. ✅ `AddressServiceTest.java`
5. ✅ `KycDocumentRepositoryTest.java`
6. ✅ `AddressRepositoryTest.java`
7. ✅ `UserRepositoryTest.java`
8. ✅ `OrganisationRepositoryTest.java`
9. ✅ `OrganisationControllerTest.java`
10. ✅ `KycDocumentControllerTest.java`

## Priority Areas for Improvement

### HIGH PRIORITY (Critical Gaps)

#### 1. **Mapper Layer** (2% coverage)
**Files to Test:**
- `OrganisationMapper.java`
- `KycDocumentMapper.java`
- `AddressMapper.java`
- `UserMapper.java`

**Why Critical:**
- Mappers handle data transformation between entities and DTOs
- Errors here can cause data corruption or loss
- Currently almost zero coverage

**Test Approach:**
```java
// Example: OrganisationMapperTest.java
@ExtendWith(MockitoExtension.class)
class OrganisationMapperTest {
    
    @InjectMocks
    private OrganisationMapperImpl organisationMapper;
    
    @Test
    void testToDto_WithFullEntity_ShouldMapAllFields() {
        // Given
        Organisation entity = createOrganisation();
        
        // When
        OrganisationDTO dto = organisationMapper.toDto(entity);
        
        // Then
        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getLegalName()).isEqualTo(entity.getLegalName());
        // ... assert all fields
    }
    
    @Test
    void testToEntity_WithNullFields_ShouldHandleGracefully() {
        // Test null handling
    }
    
    @Test
    void testUpdateEntityFromDto_ShouldPreserveId() {
        // Test update operation
    }
}
```

#### 2. **Security Layer** (17% coverage)
**Files to Test:**
- `JwtAuthenticationFilter.java`
- `JwtTokenProvider.java`
- `SecurityConfig.java`

**Why Critical:**
- Security vulnerabilities if not properly tested
- Authentication/authorization failures
- JWT token validation critical

**Test Approach:**
```java
// Example: JwtTokenProviderTest.java
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;
    
    @Test
    void testGenerateToken_ValidUser_ReturnsToken() {
        // Test token generation
    }
    
    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Test expired token validation
    }
    
    @Test
    void testGetUserIdFromToken_ValidToken_ReturnsUserId() {
        // Test token parsing
    }
}
```

#### 3. **DTO Layer** (18% coverage)
**Files to Test:**
- All DTO classes with validation annotations
- Focus on DTOs with business logic in constructors/methods

**Why Important:**
- DTOs contain validation rules
- Request/response transformations
- Data contract testing

**Test Approach:**
```java
// Example: OrganisationDTOTest.java
class OrganisationDTOTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidation_InvalidEmail_ShouldFail() {
        OrganisationDTO dto = new OrganisationDTO();
        dto.setLegalName("Test"); // valid
        dto.setEmail("invalid-email"); // invalid
        
        Set<ConstraintViolation<OrganisationDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }
    
    @Test
    void testBuilder_AllFields_ShouldCreateValidDTO() {
        // Test builder pattern
    }
}
```

### MEDIUM PRIORITY (Enhance Coverage)

#### 4. **Controller Layer** (51% coverage, 3% branch)
**Issue:** Good instruction coverage but poor branch coverage
**Files Need Branch Testing:**
- `AuthenticationController.java`
- `UserController.java`
- `AddressController.java`

**Test Approach:**
```java
// Example: Additional tests for error scenarios
@Test
void testRequestOtp_InvalidPhoneNumber_Returns400() throws Exception {
    mockMvc.perform(post("/api/auth/request-otp")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"phoneNumber\": \"invalid\"}"))
            .andExpect(status().isBadRequest());
}

@Test
void testGetUser_NonExistent_Returns404() throws Exception {
    when(userService.getUserById(999L)).thenThrow(new EntityNotFoundException());
    
    mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound());
}
```

#### 5. **Entity Layer** (18% coverage)
**Focus Areas:**
- Entity lifecycle methods (PrePersist, PreUpdate)
- Custom equals/hashCode implementations
- Relationship cascading behavior

**Test Approach:**
```java
// Example: OrganisationEntityTest.java
class OrganisationEntityTest {
    
    @Test
    void testPrePersist_ShouldSetCreatedDatetime() {
        Organisation org = new Organisation();
        org.prePersist();
        
        assertThat(org.getCreatedDatetime()).isNotNull();
    }
    
    @Test
    void testEqualsAndHashCode_SameId_ShouldBeEqual() {
        Organisation org1 = new Organisation();
        org1.setId(1L);
        Organisation org2 = new Organisation();
        org2.setId(1L);
        
        assertThat(org1).isEqualTo(org2);
        assertThat(org1.hashCode()).isEqualTo(org2.hashCode());
    }
}
```

### LOW PRIORITY (Future Enhancement)

#### 6. **Integration Tests**
- End-to-end workflow tests
- Database integration tests
- API contract tests

## Recommended Test Files to Create

### Immediate (High Priority):
1. ✅ Already exists: Service and Repository tests
2. ❌ **Create**: `OrganisationMapperTest.java`
3. ❌ **Create**: `KycDocumentMapperTest.java`
4. ❌ **Create**: `AddressMapperTest.java`
5. ❌ **Create**: `UserMapperTest.java`
6. ❌ **Create**: `JwtTokenProviderTest.java`
7. ❌ **Create**: `JwtAuthenticationFilterTest.java`
8. ❌ **Create**: `SecurityConfigTest.java`

### Secondary (Medium Priority):
9. ❌ **Enhance**: `AuthenticationControllerTest.java` (branch coverage)
10. ❌ **Enhance**: `UserControllerTest.java` (add error scenarios)
11. ❌ **Create**: `OrganisationDTOValidationTest.java`
12. ❌ **Create**: `OrganisationEntityLifecycleTest.java`

## Test Coverage Goals

| Layer | Current | Target | Priority |
|-------|---------|--------|----------|
| **Mapper** | 2% | 80%+ | 🔴 HIGH |
| **Security** | 17% | 75%+ | 🔴 HIGH |
| **DTO** | 18% | 70%+ | 🔴 HIGH |
| **Entity** | 18% | 60%+ | 🟡 MEDIUM |
| **Controller** | 51% | 80%+ | 🟡 MEDIUM |
| **Service** | 64% | 85%+ | 🟢 LOW |
| **Config** | 72% | 85%+ | 🟢 LOW |
| **Overall** | 22% | 70%+ | Target |

## Testing Best Practices

### 1. **Unit Tests**
- Fast, isolated tests
- Mock external dependencies
- Focus on single responsibility
- AAA pattern (Arrange, Act, Assert)

### 2. **Test Naming Convention**
```
test<MethodName>_<Scenario>_<ExpectedResult>
```
Examples:
- `testCreateOrganisation_ValidData_ReturnsCreatedOrganisation()`
- `testValidateToken_ExpiredToken_ReturnsFalse()`
- `testMapToDto_NullEntity_ThrowsException()`

### 3. **Coverage Metrics to Track**
- **Instruction Coverage**: Percentage of code executed
- **Branch Coverage**: Percentage of decision points tested
- **Method Coverage**: Percentage of methods called
- **Class Coverage**: Percentage of classes instantiated

### 4. **Test Data Builders**
Create test data builders for complex entities:
```java
public class OrganisationTestBuilder {
    public static Organisation.OrganisationBuilder defaultOrganisation() {
        return Organisation.builder()
            .legalName("Test Company Ltd")
            .registrationNumber("REG123")
            .statusDescription(StatusDescription.PENDING)
            // ... all required fields
    }
}
```

## Running Coverage Reports

### Generate Coverage:
```bash
mvn clean test jacoco:report
```

### View Report:
Open `target/site/jacoco/index.html` in browser

### Continuous Monitoring:
- Integrate Jacoco with CI/CD
- Set minimum coverage thresholds
- Fail builds if coverage drops

## Next Steps

1. ✅ **Create Mapper Tests** (Highest ROI - easiest to test, currently 2%)
2. ✅ **Add Security Tests** (Critical for security)
3. ✅ **Enhance Controller Tests** (Focus on branch coverage)
4. ✅ **Add DTO Validation Tests** (Important for data integrity)
5. ✅ **Add Entity Lifecycle Tests** (Medium priority)
6. 📊 **Monitor and Iterate** (Track improvement over time)

## Tools and Dependencies

### Current Test Dependencies:
- ✅ JUnit 5
- ✅ Mockito
- ✅ Spring Boot Test
- ✅ Jacoco

### Consider Adding:
- AssertJ (fluent assertions)
- Testcontainers (database integration tests)
- REST Assured (API testing)
- ArchUnit (architecture tests)

## Success Metrics

- Achieve 70%+ overall instruction coverage
- Achieve 50%+ branch coverage
- Zero critical security paths untested
- All mapper methods tested
- All validation rules tested

---

**Report Generated**: 2026-01-04
**Test Suite Status**: All tests passing ✅
**Coverage Tool**: Jacoco 0.8.11
