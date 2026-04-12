# SMS OTP Setup Guide

Complete guide to enable SMS OTP delivery to your phone number: **+447878282674**

## 🚀 Quick Start

### Prerequisites
- Twilio account (sign up at https://www.twilio.com/try-twilio)
- UK phone number verified in Twilio
- Twilio credentials (Account SID, Auth Token, Phone Number)

### Setup Steps

#### 1. Create Twilio Account
```
1. Visit: https://www.twilio.com/try-twilio
2. Sign up with your email
3. Verify your email address
4. You'll get $15 free trial credit
```

#### 2. Get Twilio Credentials
```
1. Log into Twilio Console: https://console.twilio.com/
2. Copy your Account SID (starts with AC...)
3. Copy your Auth Token (click "Show" to reveal)
4. Get a Twilio phone number:
   - Go to: Phone Numbers → Buy a Number
   - Select UK (+44) country code
   - Choose a number (costs from $1/month)
   - Buy the number
```

#### 3. Verify Your Phone Number (Trial Account)
```
During trial period, you can only send SMS to verified numbers:

1. Go to: Phone Numbers → Verified Caller IDs
2. Click "Add a new number"
3. Enter: +447878282674
4. You'll receive a verification code via voice call or SMS
5. Enter the code to verify
```

#### 4. Configure Environment Variables

**Option A: Local Development (Windows PowerShell)**
```powershell
# Set environment variables for current session
$env:SMS_ENABLED="true"
$env:TWILIO_ACCOUNT_SID="ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
$env:TWILIO_AUTH_TOKEN="your_auth_token_here"
$env:TWILIO_FROM_NUMBER="+44xxxxxxxxxx"

# Run the application
cd c:\Development\git\userManagementApi
.\start-local.ps1
```

**Option B: Permanent Environment Variables (Windows)**
```powershell
# Set permanently via System Properties
1. Press Win + X → System → Advanced system settings
2. Environment Variables → User variables
3. Add new variables:
   - SMS_ENABLED = true
   - TWILIO_ACCOUNT_SID = ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   - TWILIO_AUTH_TOKEN = your_auth_token_here
   - TWILIO_FROM_NUMBER = +44xxxxxxxxxx
4. Restart terminal/IDE
```

**Option C: application.yml (Not Recommended - Credentials in Code)**
```yaml
sms:
  enabled: true
  twilio:
    account-sid: ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    auth-token: your_auth_token_here
    from-number: +44xxxxxxxxxx
```

**Option D: GCP Cloud Run (Production)**
```powershell
# Set secrets in GCP Secret Manager
gcloud secrets create TWILIO_ACCOUNT_SID --data-file=- <<< "ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
gcloud secrets create TWILIO_AUTH_TOKEN --data-file=- <<< "your_auth_token_here"
gcloud secrets create TWILIO_FROM_NUMBER --data-file=- <<< "+44xxxxxxxxxx"

# Update Cloud Run service to use secrets
gcloud run services update fincore-npe-api \
  --update-env-vars SMS_ENABLED=true \
  --update-secrets=TWILIO_ACCOUNT_SID=TWILIO_ACCOUNT_SID:latest,TWILIO_AUTH_TOKEN=TWILIO_AUTH_TOKEN:latest,TWILIO_FROM_NUMBER=TWILIO_FROM_NUMBER:latest \
  --region=europe-west2
```

#### 5. Test SMS OTP

**Local Testing:**
```powershell
# Start the application
cd c:\Development\git\userManagementApi
.\start-local.ps1

# Request OTP (in another terminal)
curl -X POST http://localhost:8080/api/auth/otp/request `
  -H "Content-Type: application/json" `
  -d '{"phoneNumber":"+447878282674"}'

# Check your phone for SMS!
# You should receive: "Your FinCore OTP code is: XXXXXX. This code is valid for 5 minutes. Do not share this code with anyone."
```

**Verify OTP:**
```powershell
curl -X POST http://localhost:8080/api/auth/otp/verify `
  -H "Content-Type: application/json" `
  -d '{"phoneNumber":"+447878282674","otpCode":"123456"}'
```

#### 6. Verify Logs

**Check application logs for:**
```
✅ SUCCESS: "Twilio SMS service initialized successfully with from number: +44xxxxxxxxxx"
✅ SUCCESS: "SMS sent successfully to +447878282674 with SID: SMxxxxxxxxxxxxxxxx"

❌ FAILURE: "Failed to initialize Twilio: [Account not found]"
❌ FAILURE: "Failed to send SMS to +447878282674: [Unable to create record]"
```

## 📋 Troubleshooting

### Issue: SMS Not Received

**Check 1: SMS Enabled?**
```powershell
# Verify environment variable is set
echo $env:SMS_ENABLED
# Should output: true
```

**Check 2: Phone Number Format**
```
✅ CORRECT: +447878282674 (E.164 format with + prefix)
❌ WRONG: 447878282674 (missing +)
❌ WRONG: 07878282674 (national format)
❌ WRONG: +44 7878 282674 (has spaces)
```

**Check 3: Twilio Trial Restrictions**
```
Trial accounts can only send to:
- Your Twilio phone number
- Verified phone numbers in your account

Solution: Verify +447878282674 in Twilio Console
```

**Check 4: Twilio Balance**
```
1. Check account balance: https://console.twilio.com/
2. Each SMS costs ~$0.08 for UK numbers
3. Add funds if balance is low
```

### Issue: "Account not found" Error

**Solution:**
```
- Double-check TWILIO_ACCOUNT_SID starts with "AC"
- Ensure no extra spaces or quotes in environment variables
- Try copying credentials again from Twilio Console
```

### Issue: "Unable to create record" Error

**Solution:**
```
- Verify phone number is in correct E.164 format (+447878282674)
- Check that recipient number is verified (for trial accounts)
- Ensure Twilio phone number is SMS-capable (not all are)
```

### Issue: SMS Delayed

**Normal Behavior:**
```
- SMS typically arrives within 1-5 seconds
- UK networks sometimes delay by 10-30 seconds
- Check Twilio logs: https://console.twilio.com/monitor/logs/sms
```

## 🔐 Security Best Practices

### DO:
✅ Store credentials in environment variables or secret manager
✅ Use GCP Secret Manager for production
✅ Rotate Auth Token periodically
✅ Enable two-factor authentication on Twilio account
✅ Monitor usage and set spending limits
✅ Use HTTPS only for API endpoints

### DON'T:
❌ Commit credentials to Git
❌ Hardcode credentials in code
❌ Share Auth Token publicly
❌ Use trial account for production
❌ Disable SMS verification without alternative

## 💰 Twilio Pricing (UK)

```
Phone Number: ~$1.00/month
Outbound SMS: ~$0.08/message
Inbound SMS: ~$0.01/message

Estimated cost for 1000 OTPs/month: ~$81
```

### Cost Optimization:
- Use alphanumeric sender ID instead of phone number (cheaper)
- Implement rate limiting to prevent spam
- Cache OTP for retry attempts (don't generate new OTP each time)
- Set reasonable OTP expiration (current: 5 minutes)

## 📊 Monitoring

### Twilio Console
```
Monitor SMS delivery:
https://console.twilio.com/monitor/logs/sms

Track costs:
https://console.twilio.com/billing
```

### Application Logs
```powershell
# View logs (local)
Get-Content -Path "logs/application.log" -Tail 50 -Wait

# View logs (GCP Cloud Run)
gcloud run services logs read fincore-npe-api --region=europe-west2 --limit=50
```

### Metrics to Track
- SMS success rate
- SMS delivery time
- Failed SMS attempts
- Daily/monthly costs
- OTP verification rate

## 🔄 Switching Providers

If you need to switch from Twilio to another provider:

### AWS SNS
```java
// Update pom.xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-sns</artifactId>
    <version>1.12.529</version>
</dependency>

// Update SmsService.java to use AWS SNS SDK
```

### Azure Communication Services
```java
// Update pom.xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-sms</artifactId>
    <version>1.1.5</version>
</dependency>

// Update SmsService.java to use Azure SDK
```

### MessageBird (EU-focused)
```java
// Update pom.xml
<dependency>
    <groupId>com.messagebird</groupId>
    <artifactId>messagebird-api</artifactId>
    <version>6.1.0</version>
</dependency>

// Update SmsService.java to use MessageBird SDK
```

## 📞 Support

### Twilio Support
- Documentation: https://www.twilio.com/docs/sms
- Support: https://support.twilio.com/
- Community: https://www.twilio.com/community

### FinCore Support
- Check application logs for errors
- Review README.md for configuration
- Test with Postman collection: postman_security_tests.json

## ✅ Checklist

Before marking SMS OTP as complete:

- [ ] Twilio account created and verified
- [ ] UK phone number purchased in Twilio
- [ ] Phone number +447878282674 verified in Twilio
- [ ] Environment variables set (SMS_ENABLED, TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_NUMBER)
- [ ] Application restarted with new configuration
- [ ] Test OTP request sent successfully
- [ ] SMS received on +447878282674
- [ ] OTP verification successful
- [ ] Logs show "SMS sent successfully"
- [ ] Twilio console shows successful delivery
- [ ] Cost monitoring configured
- [ ] Production secrets configured in GCP (if deploying to Cloud Run)

---

**Need Help?** Check the logs first, then verify each checklist item above. Most issues are related to incorrect phone number format or unverified numbers in trial accounts.
