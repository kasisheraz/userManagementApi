# Backend Test Fix Tracker

**Status:** In Progress  
**Date Started:** March 16, 2026

## Issues Found

### 1. AmlScreeningResultRepository Method Changes
- ❌ `findByVerificationIdOrderByScreenedAtDesc(long)` → ✅ `findLatestByVerificationId(Long)`
- ❌ `findByUserIdOrderByScreenedAtDesc(long)` → ✅ `findByUser_Id(Long)`
- ❌ `findByMatchFoundTrueOrderByScreenedAtDesc()` → ✅ `findHighRiskScreenings()`
- ❌ `findByScreeningTypeOrderByScreenedAtDesc(ScreeningType)` → ✅ `findByScreeningType(ScreeningType)`
- ❌ `countByScreeningTypeAndMatchFoundTrue(ScreeningType)` → ✅ `countMatchesByScreeningType(ScreeningType)`
- ❌ `findFirstByVerificationIdOrderByScreenedAtDesc(long)` → ✅ `findLatestByVerificationId(Long)`

### 2. CustomerAnswerRepository Method Changes
- ❌ `countByUser_Id(long)` → ✅ `countByUserId(Long)`
- ❌ `findByUser_IdAndAnswerIsNotNull(long)` → ✅ `findAnswersWithQuestionDetails(Long)`
- ❌ `deleteByUser_Id(long)` → ✅ `deleteByUserId(Long)`

### 3. CustomerKycVerificationRepository Method Changes
- ❌ `findByUserIdOrderByCreatedAtDesc(long)` → ✅ `findByUser_Id(Long)` or `findLatestByUserId(Long)`
- ❌ `findFirstByUserIdOrderByCreatedAtDesc(long)` → ✅ `findLatestByUserId(Long)`
- ❌ `findFirstByUserIdAndStatusOrderByApprovedAtDesc(long, status)` → ✅ `findByUserIdAndStatus(Long, VerificationStatus)`
- ❌ `findByStatusNotIn(List)` → Need to add custom query or workaround

### 4. Entity Field Changes
**CustomerKycVerification:**
- ❌ `createdAt` → ✅ `createdDatetime`
- ❌ `setReviewedBy(User)` → ⚠️ No `reviewedBy` field exists (only `createdBy`)

### 5. Service Method Signature Changes
**AmlScreeningService:**
- ❌ `triggerSanctionsScreening(verification, user, boolean, int)` → ✅ `triggerSanctionsScreening(verification, user)`
- ❌ `triggerPepScreening(verification, user, boolean, int)` → ✅ `triggerPepScreening(verification, user)`
- ❌ `triggerAdverseMediaScreening(verification, user, boolean, int)` → ✅ `triggerAdverseMediaScreening(verification, user)`

## Files to Fix

### Priority 1 - Compilation Errors
- [ ] AmlScreening ServiceTest.java (15+ errors)
- [ ] CustomerAnswerServiceTest.java (6+ errors)
- [ ] KycVerificationServiceTest.java (10+ errors)
- [ ] QuestionnaireServiceTest.java (enum errors - partially fixed)

### Priority 2 - Other Test Files
- [ ] Review remaining 38 test files for similar issues

## Progress
- [x] Identified all breaking changes
- [x] Mapped old → new method names
- [x] Created fix tracker
- [ ] Fix AmlScreeningServiceTest
- [ ] Fix CustomerAnswerServiceTest
- [ ] Fix KycVerificationServiceTest
- [ ] Verify all tests compile
- [ ] Run full test suite
- [ ] Achieve >80% pass rate
