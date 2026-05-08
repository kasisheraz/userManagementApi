# Identity Verification Providers - Detailed Comparison

## Executive Summary

After comprehensive research of 6+ major providers, here's the recommendation:

**🏆 RECOMMENDED: SumSub** (for your use case)

**ALTERNATIVE: Onfido** (if budget allows)

---

## Detailed Provider Comparison

### 1. **SumSub** 🏆 (Recommended)

**Strengths**:
- ✅ **All-in-one solution**: KYC + AML + ongoing monitoring in one platform
- ✅ **Best value for money**: ~$1-5 per verification (volume discounts available)
- ✅ **Global coverage**: 220+ countries, 6,500+ document types
- ✅ **Built-in AML**: PEP, sanctions, adverse media screening included
- ✅ **Easy integration**: RESTful API + SDKs (React, Angular, Vue, iOS, Android)
- ✅ **Webhooks**: Real-time status updates
- ✅ **Liveness detection**: 3D face match, selfie verification
- ✅ **Customizable**: Configure verification levels, rejection rules
- ✅ **Good documentation**: Clear API docs, Postman collections
- ✅ **Free sandbox**: 100 verifications/month for testing

**Weaknesses**:
- ⚠️ UI customization limited (compared to Persona)
- ⚠️ Less brand recognition than Onfido/Jumio
- ⚠️ Support response time can be slower

**Pricing**:
- Sandbox: FREE (100/month)
- Starter: ~$1-2 per verification (basic checks)
- Growth: ~$2-5 per verification (full KYC+AML)
- Enterprise: Custom (volume discounts, dedicated support)

**Best For**: 
- Startups/SMEs needing full KYC+AML solution
- UK/EU fintech compliance
- Budget-conscious projects
- Quick integration needed

**Integration Complexity**: ⭐⭐⭐⭐☆ (4/5) - Easy

**SumSub Score**: **9/10** ✅

---

### 2. **Onfido** (Strong Alternative)

**Strengths**:
- ✅ **Market leader**: Trusted by Revolut, Zipcar, Bitstamp
- ✅ **Excellent accuracy**: AI-powered document verification
- ✅ **Strong compliance**: FCA, FinCEN, EU 5AMLD certified
- ✅ **Comprehensive checks**: ID verification, facial recognition, document checks
- ✅ **Real-time verification**: Fast processing (~30 seconds)
- ✅ **Motion capture**: Advanced liveness detection
- ✅ **Watchlist screening**: PEP, sanctions (via Dow Jones)
- ✅ **Studio UI**: White-label customization
- ✅ **Enterprise support**: Dedicated account managers

**Weaknesses**:
- ❌ **Expensive**: ~$5-10+ per verification (3-5x SumSub cost)
- ❌ **AML not included**: Need separate subscription for watchlist screening
- ⚠️ More complex pricing structure
- ⚠️ Minimum volumes required for good pricing

**Pricing**:
- Pay-as-you-go: ~$10 per check
- Growth: ~$5-7 per check (1000+ checks/month)
- Enterprise: Custom (volume discounts, SLA guarantees)
- **AML Screening**: +$2-3 per check (separate)

**Best For**:
- Medium/large enterprises with budget
- High-risk industries (crypto, remittance)
- Need brand trust/reputation
- Require advanced fraud detection

**Integration Complexity**: ⭐⭐⭐⭐☆ (4/5) - Easy

**Onfido Score**: **8.5/10** ⚠️ (Expensive but excellent)

---

### 3. **Jumio** (High-Volume Option)

**Strengths**:
- ✅ **Very fast**: Real-time verification (~15 seconds)
- ✅ **High accuracy**: AI-powered fraud detection
- ✅ **Global reach**: 200+ countries, 5,000+ ID types
- ✅ **Biometric verification**: Advanced face matching
- ✅ **Mobile-optimized**: Great mobile SDK
- ✅ **Compliance certified**: ISO 27001, SOC 2 Type II
- ✅ **Good for high volumes**: Optimized for scale

**Weaknesses**:
- ❌ **AML screening limited**: Not as comprehensive as SumSub/Onfido
- ❌ **No ongoing monitoring**: One-time checks only
- ⚠️ Pricing not transparent (must contact sales)
- ⚠️ Minimum contract volumes

**Pricing**:
- Contact sales (typically $3-7 per check)
- Volume commitments required
- Annual contracts preferred

**Best For**:
- High-volume operations (10,000+ checks/month)
- Mobile-first applications
- Speed is critical
- Don't need comprehensive AML

**Integration Complexity**: ⭐⭐⭐☆☆ (3/5) - Moderate

**Jumio Score**: **7.5/10** ⚠️ (Good for high volume)

---

### 4. **Persona** (Developer-Friendly)

**Strengths**:
- ✅ **Most customizable**: Highly flexible UI/UX
- ✅ **Developer-focused**: Best API/SDK experience
- ✅ **No-code options**: Build flows without coding
- ✅ **Transparent pricing**: Clear, predictable costs
- ✅ **US-focused**: Excellent coverage for US IDs
- ✅ **Good documentation**: Developer-friendly docs
- ✅ **Flexible workflows**: Build custom verification flows

**Weaknesses**:
- ❌ **Limited global coverage**: Weaker outside US/EU
- ❌ **No built-in AML**: Must integrate third-party
- ⚠️ Newer player (less proven at scale)
- ⚠️ Fewer document types supported

**Pricing**:
- ID Verification: $3 per check
- Selfie Verification: $1.50 per check
- Database Checks: $1-2 per check
- AML Screening: Not included (partner with Dow Jones ~$2-3)

**Best For**:
- US/Canada-focused startups
- Need heavy customization
- Developer experience priority
- Iterative development approach

**Integration Complexity**: ⭐⭐⭐⭐⭐ (5/5) - Very Easy

**Persona Score**: **7/10** ⚠️ (US-focused, no built-in AML)

---

### 5. **Veriff** (Balanced Option)

**Strengths**:
- ✅ **Fast verification**: ~6 seconds average
- ✅ **98%+ accuracy**: Strong fraud detection
- ✅ **Good global coverage**: 190+ countries
- ✅ **Video verification**: Human-in-the-loop option
- ✅ **Reasonable pricing**: $2-4 per verification
- ✅ **Good compliance**: GDPR, ISO, SOC2 certified

**Weaknesses**:
- ⚠️ AML screening basic (not as comprehensive)
- ⚠️ Limited ongoing monitoring
- ⚠️ UI less customizable

**Pricing**:
- Growth: ~$2-4 per verification
- Enterprise: Custom pricing

**Best For**:
- EU-focused operations
- Need speed + accuracy balance
- Moderate budget

**Integration Complexity**: ⭐⭐⭐⭐☆ (4/5) - Easy

**Veriff Score**: **7.5/10** ✅ (Good middle ground)

---

### 6. **Stripe Identity** (If Using Stripe)

**Strengths**:
- ✅ **Seamless if using Stripe**: One integration
- ✅ **Simple pricing**: $1.50 per verification
- ✅ **Easy setup**: Works with Stripe account
- ✅ **Good for payments KYC**: Built for payment flows
- ✅ **Developer-friendly**: Stripe-quality docs

**Weaknesses**:
- ❌ **NO AML screening**: Document verification only
- ❌ **Limited countries**: ~30 countries supported
- ❌ **Basic features**: No advanced fraud detection
- ❌ **Not comprehensive**: Need additional AML solution

**Pricing**:
- $1.50 per verification
- No monthly fees

**Best For**:
- Already using Stripe
- Simple document verification needs
- US/EU markets only
- Don't need AML

**Integration Complexity**: ⭐⭐⭐⭐⭐ (5/5) - Very Easy (if using Stripe)

**Stripe Identity Score**: **6/10** ❌ (Too basic for comprehensive KYC)

---

### 7. **Build Your Own** (AWS Rekognition + Textract)

**Strengths**:
- ✅ Full control over data
- ✅ No per-check fees (AWS usage only)
- ✅ Customizable to exact needs

**Weaknesses**:
- ❌ **High development cost**: 3-6 months, 2-3 developers
- ❌ **No compliance guarantees**: You own the risk
- ❌ **Ongoing maintenance**: Security, updates, regulations
- ❌ **No AML built-in**: Must integrate third-party
- ❌ **Lower accuracy**: Vs. specialized providers
- ❌ **Regulatory risk**: Non-compliance penalties

**Total Cost**: $50,000-150,000 (development) + $5,000-20,000/year (maintenance)

**Build Your Own Score**: **3/10** ❌ (NOT RECOMMENDED)

---

## Side-by-Side Comparison

| Feature | SumSub 🏆 | Onfido | Jumio | Persona | Veriff | Stripe Identity |
|---------|----------|--------|-------|---------|--------|-----------------|
| **Price per Check** | $1-5 | $5-10+ | $3-7 | $3-5 | $2-4 | $1.50 |
| **AML Screening** | ✅ Included | ❌ Extra $2-3 | ⚠️ Basic | ❌ Not included | ⚠️ Basic | ❌ No |
| **Global Coverage** | 220+ countries | 195+ | 200+ | US/EU focus | 190+ | ~30 countries |
| **Integration Ease** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Liveness Detection** | ✅ Yes | ✅ Advanced | ✅ Yes | ✅ Yes | ✅ Yes | ⚠️ Basic |
| **Ongoing Monitoring** | ✅ Yes | ✅ Yes | ❌ No | ❌ No | ⚠️ Limited | ❌ No |
| **Free Sandbox** | ✅ 100/month | ✅ Limited | ✅ Limited | ✅ Yes | ✅ Limited | ✅ Yes |
| **Webhooks** | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **Customization** | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| **Support Quality** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Compliance** | ✅ Strong | ✅ Excellent | ✅ Good | ✅ Good | ✅ Strong | ⚠️ Basic |
| **Best For** | SME, Cost-effective | Enterprise, Brand trust | High volume | US startups, Custom | EU operations | Stripe users only |

---

## Detailed Cost Comparison (1,000 verifications/month)

| Provider | Base Cost | AML Cost | Total/Month | Total/Year |
|----------|-----------|----------|-------------|------------|
| **SumSub** | $2,000-3,000 | Included | **$2,000-3,000** | **$24,000-36,000** |
| **Onfido** | $5,000-7,000 | +$2,000-3,000 | **$7,000-10,000** | **$84,000-120,000** |
| **Jumio** | $3,000-5,000 | Limited | **$3,000-5,000** | **$36,000-60,000** |
| **Persona** | $3,000 | +$2,000-3,000 | **$5,000-6,000** | **$60,000-72,000** |
| **Veriff** | $2,000-4,000 | Limited | **$2,000-4,000** | **$24,000-48,000** |
| **Stripe** | $1,500 | Not available | **$1,500** (NO AML ❌) | **$18,000** |

**Winner for Cost-Effectiveness**: **SumSub** 🏆 ($24,000-36,000/year with full KYC+AML)

---

## Feature Comparison Matrix

### Identity Verification Features

| Feature | SumSub | Onfido | Jumio | Persona | Veriff | Stripe |
|---------|--------|--------|-------|---------|--------|--------|
| Document Verification | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Facial Recognition | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ |
| Liveness Detection | ✅ 3D | ✅ Advanced | ✅ | ✅ | ✅ | ⚠️ Basic |
| Document Types | 6,500+ | 4,500+ | 5,000+ | 2,000+ | 4,000+ | 500+ |
| Video Verification | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ |
| Age Verification | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| Address Verification | ✅ | ✅ | ⚠️ | ⚠️ | ⚠️ | ❌ |

### AML/Compliance Features

| Feature | SumSub | Onfido | Jumio | Persona | Veriff | Stripe |
|---------|--------|--------|-------|---------|--------|--------|
| PEP Screening | ✅ Included | ✅ Extra cost | ⚠️ Basic | ❌ | ⚠️ Basic | ❌ |
| Sanctions Lists | ✅ Included | ✅ Extra cost | ⚠️ Basic | ❌ | ⚠️ Basic | ❌ |
| Adverse Media | ✅ Included | ✅ Extra cost | ❌ | ❌ | ❌ | ❌ |
| Ongoing Monitoring | ✅ Yes | ✅ Yes | ❌ | ❌ | ⚠️ Limited | ❌ |
| Risk Scoring | ✅ Yes | ✅ Yes | ✅ | ⚠️ Custom | ✅ | ❌ |
| Case Management | ✅ Yes | ✅ Advanced | ✅ | ⚠️ Basic | ✅ | ❌ |

### Technical Features

| Feature | SumSub | Onfido | Jumio | Persona | Veriff | Stripe |
|---------|--------|--------|-------|---------|--------|--------|
| REST API | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Webhooks | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| React SDK | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Mobile SDKs | ✅ iOS/Android | ✅ iOS/Android | ✅ iOS/Android | ✅ iOS/Android | ✅ iOS/Android | ✅ iOS/Android |
| Sandbox Environment | ✅ Free | ✅ Limited | ✅ Limited | ✅ Free | ✅ Limited | ✅ Free |
| Postman Collection | ✅ | ✅ | ⚠️ | ✅ | ⚠️ | ✅ |
| GraphQL API | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |

---

## Recommendation by Use Case

### **For UK/EU Fintech (Your Case)** 🏆
**Recommended: SumSub**
- Comprehensive KYC+AML in one platform
- Best value for money ($24-36k/year for full solution)
- Strong EU compliance (GDPR, 5AMLD)
- Easy integration
- Free sandbox for testing

**Alternative: Onfido** (if budget allows)
- Market leader, stronger brand trust
- Better for regulated industries (banking, crypto)
- More expensive but worth it for high-risk sectors

---

### **For US-Focused Startups**
**Recommended: Persona**
- Best developer experience
- Highly customizable
- Good US ID coverage
- But need separate AML solution

---

### **For High-Volume Operations (10k+ checks/month)**
**Recommended: Jumio**
- Optimized for scale
- Very fast processing
- Volume discounts
- But AML screening limited

---

### **For Payment Processors Already Using Stripe**
**Recommended: Stripe Identity**
- Easiest integration
- Lowest cost ($1.50/check)
- But NO AML screening (not suitable for full KYC)

---

## Final Recommendation for Your Project

### 🏆 **Go with SumSub** - Here's Why:

1. **All-in-One Solution**: KYC + AML + ongoing monitoring in one platform
   - Eliminates need for multiple integrations
   - Single vendor relationship
   - Unified pricing

2. **Best Value**: $24,000-36,000/year for full solution
   - Onfido would cost $84,000-120,000/year for same features
   - Saves $48,000-84,000/year vs. Onfido

3. **UK/EU Compliance**: Built with EU regulations in mind
   - GDPR compliant
   - 5AMLD ready
   - FCA requirements covered

4. **Quick Implementation**: 2-3 weeks vs. 4-6 weeks for others
   - Good documentation
   - Free sandbox
   - Working code samples available

5. **Scalability**: Handles 50-50,000+ checks/month
   - Start small, scale up
   - No minimum volumes
   - Pay-as-you-grow pricing

6. **Low Risk**: Free sandbox testing
   - Test thoroughly before paying
   - 100 free verifications/month
   - No credit card required for sandbox

### **When to Consider Alternatives:**

**Choose Onfido if**:
- You're a regulated institution (bank, crypto exchange)
- Brand trust is critical for fundraising
- Budget is not constrained ($80k+/year available)
- Need enterprise SLA guarantees

**Choose Jumio if**:
- You expect 10,000+ verifications/month immediately
- Speed is absolutely critical (<15 seconds)
- You don't need comprehensive AML

**Choose Persona if**:
- You're US-only, not expanding globally
- You need heavy UI/UX customization
- Developer experience is top priority

**Don't Choose**:
- ❌ Stripe Identity (no AML - not suitable for financial services KYC)
- ❌ Build your own (high risk, high cost, compliance issues)

---

## Implementation Recommendation

### Phase 1: Start with SumSub (Weeks 1-4)
1. Create sandbox account (Day 1)
2. Build integration (Week 1-2)
3. Test with sandbox data (Week 3)
4. Deploy to NPE (Week 4)

### Phase 2: Production Rollout (Weeks 5-6)
1. Create production account
2. Configure verification levels
3. Set up webhooks
4. Deploy to UAT, then Production

### Phase 3: Monitor & Optimize (Ongoing)
1. Track success/failure rates
2. Optimize verification flows
3. Review AML hit rates
4. Consider upgrades if needed

**Total Time to Production**: 6 weeks
**Total Initial Cost**: $0 (sandbox) → $24-36k/year (production)

---

## Risk Mitigation

### Vendor Lock-in Risk: LOW ✅
- Standard REST API
- Can migrate to another provider if needed
- Data export available
- Estimated migration effort: 2-3 weeks

### Compliance Risk: LOW ✅
- SumSub certified for UK/EU regulations
- Regular audits
- Compliance team support

### Cost Risk: LOW ✅
- Transparent pricing
- No hidden fees
- Volume discounts available
- Can cap monthly spend

### Technical Risk: LOW ✅
- Proven at scale (millions of checks)
- 99.9% uptime SLA
- Good documentation
- Active support

---

## Conclusion

**✅ Stick with SumSub**

It's the best choice for your use case:
- Comprehensive KYC+AML solution
- Best value for money
- Quick implementation
- Low risk
- Scalable
- UK/EU compliant

You can confidently proceed with SumSub integration as outlined in the implementation plan.

**Next Steps**:
1. Create SumSub sandbox account: https://cockpit.sumsub.com/
2. Request API credentials
3. Start NPE development
4. Test with sandbox (free 100 checks/month)
5. Move to production when ready

---

**Document Version**: 1.0  
**Last Updated**: May 8, 2026  
**Review Recommendation**: Revisit in 12 months or if verification volume exceeds 10,000/month
