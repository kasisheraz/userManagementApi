package com.fincore.usermgmt.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Service for sending SMS messages via Twilio.
 * 
 * To enable SMS sending, set the following environment variables:
 * - SMS_ENABLED=true
 * - TWILIO_ACCOUNT_SID=your_account_sid
 * - TWILIO_AUTH_TOKEN=your_auth_token
 * - TWILIO_FROM_NUMBER=+447xxxxxxxxxx (your Twilio phone number)
 * 
 * Phone numbers must be in E.164 format (e.g., +447878282674 for UK numbers)
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "sms.enabled", havingValue = "true", matchIfMissing = false)
public class SmsService {

    @Value("${sms.twilio.account-sid}")
    private String accountSid;

    @Value("${sms.twilio.auth-token}")
    private String authToken;

    @Value("${sms.twilio.from-number}")
    private String fromNumber;

    private boolean twilioInitialized = false;

    /**
     * Initialize Twilio connection.
     * Called lazily on first SMS send.
     */
    private void initializeTwilio() {
        if (!twilioInitialized) {
            try {
                Twilio.init(accountSid, authToken);
                twilioInitialized = true;
                log.info("Twilio SMS service initialized successfully with from number: {}", fromNumber);
            } catch (Exception e) {
                log.error("Failed to initialize Twilio: {}", e.getMessage());
                throw new RuntimeException("SMS service initialization failed", e);
            }
        }
    }

    /**
     * Send an SMS message to the specified phone number.
     *
     * @param toPhoneNumber Recipient phone number in E.164 format (e.g., +447878282674)
     * @param messageText   Message content to send
     * @throws RuntimeException if SMS sending fails
     */
    public void sendSms(String toPhoneNumber, String messageText) {
        try {
            // Lazy initialization of Twilio
            initializeTwilio();

            // Validate phone number format (basic check for E.164)
            if (!toPhoneNumber.startsWith("+")) {
                log.warn("Phone number {} does not start with '+'. Adding + prefix.", toPhoneNumber);
                toPhoneNumber = "+" + toPhoneNumber;
            }

            // Send SMS via Twilio
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromNumber),
                    messageText
            ).create();

            log.info("SMS sent successfully to {} with SID: {}", toPhoneNumber, message.getSid());

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    /**
     * Send OTP code via SMS.
     *
     * @param phoneNumber Recipient phone number in E.164 format
     * @param otpCode     The OTP code to send
     */
    public void sendOtp(String phoneNumber, String otpCode) {
        String message = String.format(
                "Your FinCore OTP code is: %s. This code is valid for 5 minutes. Do not share this code with anyone.",
                otpCode
        );
        sendSms(phoneNumber, message);
    }
}
