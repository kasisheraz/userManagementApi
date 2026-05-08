# SumSub Identity Verification Integration Guide

## 📋 Overview

SumSub (Sum&Substance) is a comprehensive identity verification platform that provides:
- Document verification (ID cards, passports, driver's licenses)
- Biometric verification (liveness detection, face matching)
- AML screening (PEP, sanctions, adverse media)
- Global coverage (220+ countries, 6500+ document types)
- RESTful API + SDKs (JS, iOS, Android, React Native)

## 🔑 Setup & Configuration

### 1. Create SumSub Account

**Sandbox (Development)**:
- Sign up: https://cockpit.sumsub.com/
- Environment: Test Mode
- Free for testing (limited volume)

**Production**:
- Contact sales for pricing (volume-based ~$1-5 per verification)
- Setup production credentials
- Configure compliance requirements

### 2. Get API Credentials

After account creation:
1. Navigate to **Settings** → **Credentials**
2. Generate API credentials:
   - **App Token**: `sbx:YOUR_APP_TOKEN`
   - **Secret Key**: `YOUR_SECRET_KEY`
3. Note the **Base URL**:
   - Sandbox: `https://api.sumsub.com`
   - Production: `https://api.sumsub.com`

### 3. Configure in Application

**Environment Variables** (`application-uat.yml`):
```yaml
sumsub:
  enabled: true
  base-url: https://api.sumsub.com
  app-token: ${SUMSUB_APP_TOKEN}
  secret-key: ${SUMSUB_SECRET_KEY}
  level-name: basic-kyc-level
  webhook-secret: ${SUMSUB_WEBHOOK_SECRET}
```

## 🏗️ Architecture Overview

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   User      │         │   Backend    │         │   SumSub    │
│   (React)   │         │   (Spring)   │         │   Platform  │
└─────┬───────┘         └──────┬───────┘         └──────┬──────┘
      │                        │                        │
      │  1. Start KYC          │                        │
      │───────────────────────>│                        │
      │                        │  2. Create Applicant   │
      │                        │───────────────────────>│
      │                        │<───────────────────────│
      │                        │  3. Return Token       │
      │<───────────────────────│                        │
      │                        │                        │
      │  4. Load SumSub SDK    │                        │
      │───────────────────────────────────────────────>│
      │                        │                        │
      │  5. User Uploads Docs  │                        │
      │───────────────────────────────────────────────>│
      │                        │                        │
      │  6. Liveness Check     │                        │
      │───────────────────────────────────────────────>│
      │                        │                        │
      │                        │  7. Webhook: Status    │
      │                        │<───────────────────────│
      │                        │                        │
      │  8. Poll Status        │                        │
      │───────────────────────>│                        │
      │<───────────────────────│                        │
```

## 🔌 Backend Integration

### Service Layer

**`SumSubIntegrationService.java`**:
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class SumSubIntegrationService {

    @Value("${sumsub.base-url}")
    private String baseUrl;
    
    @Value("${sumsub.app-token}")
    private String appToken;
    
    @Value("${sumsub.secret-key}")
    private String secretKey;
    
    private final RestTemplate restTemplate;
    
    /**
     * Create a new applicant in SumSub
     */
    public SumSubApplicantResponse createApplicant(User user) {
        String url = baseUrl + "/resources/applicants";
        
        SumSubApplicantRequest request = SumSubApplicantRequest.builder()
            .externalUserId(user.getId().toString())
            .email(user.getEmail())
            .phone(user.getPhoneNumber())
            .fixedInfo(FixedInfo.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dob(user.getDateOfBirth().toString())
                .build())
            .build();
        
        HttpHeaders headers = createHeaders("POST", "/resources/applicants");
        HttpEntity<SumSubApplicantRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<SumSubApplicantResponse> response = 
            restTemplate.exchange(url, HttpMethod.POST, entity, SumSubApplicantResponse.class);
        
        return response.getBody();
    }
    
    /**
     * Generate access token for SDK
     */
    public String generateAccessToken(String applicantId, String levelName) {
        String url = baseUrl + "/resources/accessTokens";
        
        Map<String, String> request = Map.of(
            "userId", applicantId,
            "levelName", levelName,
            "ttlInSecs", "600"
        );
        
        HttpHeaders headers = createHeaders("POST", "/resources/accessTokens");
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<Map> response = 
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        
        return (String) response.getBody().get("token");
    }
    
    /**
     * Get applicant status
     */
    public SumSubApplicantStatus getApplicantStatus(String applicantId) {
        String url = baseUrl + "/resources/applicants/" + applicantId + "/status";
        
        HttpHeaders headers = createHeaders("GET", "/resources/applicants/" + applicantId + "/status");
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        ResponseEntity<SumSubApplicantStatus> response = 
            restTemplate.exchange(url, HttpMethod.GET, entity, SumSubApplicantStatus.class);
        
        return response.getBody();
    }
    
    /**
     * Create authorization headers with HMAC signature
     */
    private HttpHeaders createHeaders(String method, String path) {
        long timestamp = System.currentTimeMillis() / 1000;
        String signature = generateSignature(method, path, timestamp);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-App-Token", appToken);
        headers.set("X-App-Access-Ts", String.valueOf(timestamp));
        headers.set("X-App-Access-Sig", signature);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        return headers;
    }
    
    /**
     * Generate HMAC-SHA256 signature
     */
    private String generateSignature(String method, String path, long timestamp) {
        try {
            String message = timestamp + method.toUpperCase() + path;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(message.getBytes());
            return Hex.encodeHexString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
}
```

### Webhook Handling

**`SumSubWebhookController.java`**:
```java
@RestController
@RequestMapping("/api/webhooks/sumsub")
@RequiredArgsConstructor
@Slf4j
public class SumSubWebhookController {

    private final KycVerificationService kycService;
    private final SumSubIntegrationService sumsubService;
    
    @Value("${sumsub.webhook-secret}")
    private String webhookSecret;
    
    /**
     * Receive SumSub webhook notifications
     */
    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody SumSubWebhookPayload payload,
            @RequestHeader("X-Payload-Digest") String signature) {
        
        log.info("Received SumSub webhook: type={}, applicantId={}", 
            payload.getType(), payload.getApplicantId());
        
        // Verify signature
        if (!verifySignature(payload, signature)) {
            log.warn("Invalid webhook signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Process webhook based on type
        switch (payload.getType()) {
            case "applicantCreated":
                handleApplicantCreated(payload);
                break;
            case "applicantReviewed":
                handleApplicantReviewed(payload);
                break;
            case "applicantPending":
                handleApplicantPending(payload);
                break;
            default:
                log.debug("Unhandled webhook type: {}", payload.getType());
        }
        
        return ResponseEntity.ok().build();
    }
    
    private void handleApplicantReviewed(SumSubWebhookPayload payload) {
        String applicantId = payload.getApplicantId();
        String reviewStatus = payload.getReviewStatus();
        
        // Update KYC verification status
        CustomerKycVerification verification = 
            kycService.getBySum SubApplicantId(applicantId);
        
        VerificationStatus status = mapReviewStatus(reviewStatus);
        kycService.updateVerificationStatus(
            verification.getVerificationId(),
            status,
            null,
            payload.toString()
        );
    }
    
    private VerificationStatus mapReviewStatus(String reviewStatus) {
        return switch (reviewStatus) {
            case "completed" -> VerificationStatus.APPROVED;
            case "rejected" -> VerificationStatus.REJECTED;
            default -> VerificationStatus.PENDING;
        };
    }
    
    private boolean verifySignature(SumSubWebhookPayload payload, String signature) {
        // Implement HMAC verification
        String computed = sumsubService.computeWebhookSignature(payload);
        return computed.equals(signature);
    }
}
```

## 🎨 Frontend Integration

### SumSub SDK Integration

**Install SDK**:
```bash
npm install @sumsub/websdk @sumsub/websdk-react
```

**Component** (`KycStep2Sumsub.tsx`):
```tsx
import React, { useEffect, useState } from 'react';
import SumsubWebSdk from '@sumsub/websdk-react';

export const KycStep2Sumsub: React.FC = () => {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [applicantEmail, setApplicantEmail] = useState<string>('');
  const [applicantPhone, setApplicantPhone] = useState<string>('');

  useEffect(() => {
    // Get access token from backend
    const fetchAccessToken = async () => {
      const response = await fetch('/api/kyc/sumsub/token', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('jwt')}`
        }
      });
      const data = await response.json();
      setAccessToken(data.token);
      setApplicantEmail(data.email);
      setApplicantPhone(data.phone);
    };
    
    fetchAccessToken();
  }, []);

  const handleMessage = (type: string, payload: any) => {
    console.log('SumSub SDK message:', type, payload);
    
    if (type === 'idCheck.onApplicantLoaded') {
      console.log('Applicant loaded');
    } else if (type === 'idCheck.onApplicantSubmitted') {
      console.log('Documents submitted');
      // Proceed to next step
      window.location.href = '/kyc/step3';
    } else if (type === 'idCheck.onError') {
      console.error('SumSub error:', payload);
    }
  };

  if (!accessToken) {
    return <div>Loading verification...</div>;
  }

  return (
    <div className="kyc-sumsub-container">
      <h2>Step 2: Identity Verification</h2>
      <p>Please upload your identification documents</p>
      
      <SumsubWebSdk
        accessToken={accessToken}
        expirationHandler={() => {
          // Refresh token if expired
          return Promise.resolve(accessToken);
        }}
        config={{
          lang: 'en',
          email: applicantEmail,
          phone: applicantPhone,
          theme: 'light'
        }}
        options={{
          addViewportTag: false,
          adaptIframeHeight: true
        }}
        onMessage={handleMessage}
        onError={(error) => console.error('SDK Error:', error)}
      />
    </div>
  );
};
```

## 📊 API Endpoints Reference

### SumSub REST API

**Base URL**: `https://api.sumsub.com`

#### 1. Create Applicant
```http
POST /resources/applicants
Headers:
  X-App-Token: YOUR_APP_TOKEN
  X-App-Access-Ts: UNIX_TIMESTAMP
  X-App-Access-Sig: HMAC_SIGNATURE
  Content-Type: application/json

Body:
{
  "externalUserId": "user_123",
  "email": "user@example.com",
  "phone": "+447700900000",
  "fixedInfo": {
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-01-01"
  }
}

Response:
{
  "id": "5f123...",
  "createdAt": "2024-01-01T10:00:00Z",
  "key": "ABCDE",
  "clientId": "fincore",
  "inspectionId": "5f123...",
  "externalUserId": "user_123"
}
```

#### 2. Generate Access Token
```http
POST /resources/accessTokens
Body:
{
  "userId": "5f123...",
  "levelName": "basic-kyc-level",
  "ttlInSecs": 600
}

Response:
{
  "token": "act.eyJ...",
  "userId": "5f123..."
}
```

#### 3. Get Applicant Status
```http
GET /resources/applicants/{applicantId}/status

Response:
{
  "id": "5f123...",
  "reviewStatus": "completed",
  "reviewResult": {
    "reviewAnswer": "GREEN",
    "rejectLabels": [],
    "reviewRejectType": null
  }
}
```

## 🔔 Webhook Events

SumSub sends webhooks for these events:

| Event | Description | Action |
|-------|-------------|--------|
| `applicantCreated` | New applicant created | Log creation |
| `applicantPending` | Awaiting review | Update status to PENDING |
| `applicantReviewed` | Review completed | Update to APPROVED/REJECTED |
| `applicantOnHold` | On hold for manual review | Update status, notify admin |
| `videoIdentCompleted` | Video verification done | Log completion |

**Webhook Payload**:
```json
{
  "applicantId": "5f123...",
  "inspectionId": "5f456...",
  "correlationId": "req_123",
  "externalUserId": "user_123",
  "type": "applicantReviewed",
  "reviewStatus": "completed",
  "reviewResult": {
    "reviewAnswer": "GREEN"
  },
  "createdAt": "2024-01-01T10:00:00Z"
}
```

## 🧪 Testing

### Unit Tests

```java
@Test
void testCreateApplicant() {
    // Mock user
    User user = User.builder()
        .id(1L)
        .email("test@example.com")
        .phoneNumber("+447700900000")
        .firstName("Test")
        .lastName("User")
        .build();
    
    // Mock RestTemplate
    when(restTemplate.exchange(
        anyString(), 
        eq(HttpMethod.POST), 
        any(), 
        eq(SumSubApplicantResponse.class)
    )).thenReturn(ResponseEntity.ok(mockResponse));
    
    // Test
    SumSubApplicantResponse response = sumsubService.createApplicant(user);
    
    assertNotNull(response);
    assertEquals("5f123...", response.getId());
}
```

### Integration Tests

```java
@Test
@Sql("/test-data-kyc.sql")
void testKycWorkflowWithSumSub() {
    // Create user
    User user = createTestUser();
    
    // Start KYC
    CustomerKycVerification kyc = kycService.submitVerification(user, VerificationLevel.BASIC);
    assertEquals(VerificationStatus.PENDING, kyc.getStatus());
    
    // Create SumSub applicant (mocked in test)
    SumSubApplicantResponse applicant = sumsubService.createApplicant(user);
    
    // Update verification with SumSub ID
    kyc.setSumsubApplicantId(applicant.getId());
    kycService.save(kyc);
    
    // Simulate webhook
    SumSubWebhookPayload webhook = createApprovedWebhook(applicant.getId());
    webhookController.handleWebhook(webhook, "valid_signature");
    
    // Verify status updated
    CustomerKycVerification updated = kycService.getVerificationById(kyc.getVerificationId());
    assertEquals(VerificationStatus.APPROVED, updated.getStatus());
}
```

## 🔐 Security Best Practices

1. **API Credentials**: Never commit to Git, use environment variables
2. **Webhook Signature**: Always verify webhook signatures
3. **HTTPS Only**: Use HTTPS for all API calls
4. **Token Expiry**: Set reasonable TTL for access tokens (600s recommended)
5. **Rate Limiting**: Implement rate limiting on webhook endpoints
6. **Logging**: Log all API interactions (sanitize PII)
7. **Error Handling**: Don't expose internal errors to client

## 📚 Resources

- **Documentation**: https://developers.sumsub.com/
- **API Reference**: https://developers.sumsub.com/api-reference/
- **SDK Documentation**: https://developers.sumsub.com/msdk-web/
- **Postman Collection**: https://www.postman.com/sumsub
- **Support**: support@sumsub.com

## 💰 Pricing Overview

| Plan | Price | Volume | Features |
|------|-------|--------|----------|
| **Sandbox** | Free | 100/month | Testing only |
| **Starter** | ~$1/check | 0-1K/month | Basic KYC + AML |
| **Growth** | ~$2-3/check | 1K-10K/month | Full features |
| **Enterprise** | Custom | 10K+/month | Custom SLA, dedicated support |

*Prices are approximate and vary by region/volume*

---

**Next Steps**:
1. Create sandbox account
2. Get API credentials
3. Implement backend service
4. Integrate frontend SDK
5. Setup webhooks
6. Test end-to-end flow

