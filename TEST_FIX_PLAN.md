# Comprehensive Test Fix Plan

## Test Failure Summary
- **Total Tests:** 684
- **Failures:** 43
- **Errors:** 21
- **Total Issues:** 64

## Test Categories & Fix Strategy

### 1. Controller Tests - 500 Internal Server Errors (35 failures)
**Root Cause:** Full Spring context loading but runtime exceptions in controllers
**Affected Classes:**
- OrganisationControllerTest: 12 failures (all 500 errors)
- OrganisationControllerEdgeCaseTest: 10 failures (all 500 errors)
- KycWorkflowControllerTest: 9 errors (all 500 errors)
- UserControllerEdgeCaseTest: 2 failures (expecting 204 but got 404)

**Fix Strategy:** 
- Check for missing bean dependencies
- Verify MockBean configurations
- Add missing test data setup

### 2. Service Layer Tests - Assertion Mismatches (11 failures)
**Affected Classes:**
- QuestionnaireServiceTest: 2 failures (expected 1 but was 0)
- AmlScreeningServiceTest: 1 failure
- KycVerificationServiceTest: 1 failure
- KycWorkflowServiceTest: 1 failure + 1 error
- OrganisationServiceTest: 2 errors
- UserServiceTest: 1 failure
- CustomerAnswerServiceTest: Already fixed (20/20 passing)

**Fix Strategy:**
- Fix test data setup
- Ensure repositories return correct data
- Fix mock configurations

### 3. Security Tests - Phone Number Formatting (12 failures)
**Affected Classes:**
- JwtAuthenticationFilterTest: 6 failures + 6 errors

**Fix Strategy:**
- Fix phone number assertions
- Review unnecessary stubbing warnings
- Update test expectations

### 4. Integration Tests (5 failures)
**Affected Classes:**
- ApiIntegrationTest: 4 failures + 3 errors
- UserSecurityIntegrationTest: 1 failure

**Fix Strategy:**
- Ensure full context loads correctly
- Verify integration test data
- Check authentication setup

### 5. Entity & Mapper Tests (2 failures)
**Affected Classes:**
- CustomerKycVerificationTest: 1 failure
- UserMapperTest: 1 failure

**Fix Strategy:**
- Fix entity validation tests
- Update mapper test expectations

## Execution Plan

1. ✅ Fix CustomerAnswerServiceTest - DONE (20/20 passing)
2. Fix Controller 500 errors (highest impact - 35 tests)
3. Fix Service layer tests (11 tests)
4. Fix Security tests (12 tests)
5. Fix Integration tests (5 tests)
6. Fix Entity & Mapper tests (2 tests)
7. Run full test suite and verify all green
8. Commit and push to npe (quality gate enabled)

## Progress Tracking
- [x] CustomerAnswerServiceTest: 20/20 ✅
- [ ] Controller Tests: 0/35
- [ ] Service Tests: 0/11
- [ ] Security Tests: 0/12
- [ ] Integration Tests: 0/5
- [ ] Entity & Mapper Tests: 0/2

**Target:** 684/684 tests passing (100%)
**Current:** 620/684 tests passing (90.6%)
**Remaining:** 64 tests to fix
