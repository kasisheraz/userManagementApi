package com.fincore.usermgmt.service;

import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.VerificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

/**
 * Service for sending email notifications related to KYC workflow status changes
 * Only enabled when mail server is configured (spring.mail.host property exists)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.mail.host")
public class KycEmailNotificationService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@fincore.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.support.email:support@fincore.com}")
    private String supportEmail;

    /**
     * Send email notification when KYC verification is submitted
     */
    @Async
    public void sendKycSubmittedNotification(CustomerKycVerification verification) {
        try {
            User user = verification.getUser();
            
            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("lastName", user.getLastName());
            context.setVariable("verificationId", verification.getVerificationId());
            context.setVariable("verificationLevel", verification.getVerificationLevel().name());
            context.setVariable("submittedAt", verification.getSubmittedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            context.setVariable("statusUrl", frontendUrl + "/kyc/status/" + verification.getVerificationId());
            context.setVariable("supportEmail", supportEmail);

            String htmlContent = templateEngine.process("email/kyc-submitted", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "KYC Verification Submitted - FINCORE",
                    htmlContent
            );

            log.info("Sent KYC submitted notification to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send KYC submitted notification", e);
        }
    }

    /**
     * Send email notification when KYC verification is approved
     */
    @Async
    public void sendKycApprovedNotification(CustomerKycVerification verification) {
        try {
            User user = verification.getUser();

            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("lastName", user.getLastName());
            context.setVariable("verificationId", verification.getVerificationId());
            context.setVariable("verificationLevel", verification.getVerificationLevel().name());
            context.setVariable("approvedAt", verification.getReviewedAt() != null 
                    ? verification.getReviewedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                    : "N/A");
            context.setVariable("dashboardUrl", frontendUrl + "/dashboard");
            context.setVariable("riskLevel", verification.getRiskLevel() != null ? verification.getRiskLevel().name() : "N/A");

            String htmlContent = templateEngine.process("email/kyc-approved", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "KYC Verification Approved - FINCORE",
                    htmlContent
            );

            log.info("Sent KYC approved notification to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send KYC approved notification", e);
        }
    }

    /**
     * Send email notification when KYC verification is rejected
     */
    @Async
    public void sendKycRejectedNotification(CustomerKycVerification verification, String reason) {
        try {
            User user = verification.getUser();

            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("lastName", user.getLastName());
            context.setVariable("verificationId", verification.getVerificationId());
            context.setVariable("rejectedAt", verification.getReviewedAt() != null
                    ? verification.getReviewedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                    : "N/A");
            context.setVariable("reason", reason != null ? reason : "Additional information required");
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("restartUrl", frontendUrl + "/kyc/start");

            String htmlContent = templateEngine.process("email/kyc-rejected", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "KYC Verification - Additional Information Required - FINCORE",
                    htmlContent
            );

            log.info("Sent KYC rejected notification to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send KYC rejected notification", e);
        }
    }

    /**
     * Send email notification when KYC verification is under review
     */
    @Async
    public void sendKycUnderReviewNotification(CustomerKycVerification verification) {
        try {
            User user = verification.getUser();

            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("lastName", user.getLastName());
            context.setVariable("verificationId", verification.getVerificationId());
            context.setVariable("reviewStartedAt", verification.getReviewedAt() != null
                    ? verification.getReviewedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                    : "Recently");
            context.setVariable("statusUrl", frontendUrl + "/kyc/status/" + verification.getVerificationId());
            context.setVariable("estimatedTime", "24-48 hours");

            String htmlContent = templateEngine.process("email/kyc-under-review", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "KYC Verification Under Review - FINCORE",
                    htmlContent
            );

            log.info("Sent KYC under review notification to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send KYC under review notification", e);
        }
    }

    /**
     * Send status change notification based on new status
     */
    @Async
    public void sendStatusChangeNotification(CustomerKycVerification verification, VerificationStatus oldStatus, VerificationStatus newStatus) {
        if (oldStatus == newStatus) {
            return;
        }

        log.info("KYC status changed for verification {}: {} -> {}", 
                verification.getVerificationId(), oldStatus, newStatus);

        switch (newStatus) {
            case PENDING:
                if (oldStatus == null || oldStatus != VerificationStatus.PENDING) {
                    sendKycUnderReviewNotification(verification);
                }
                break;
            case APPROVED:
                sendKycApprovedNotification(verification);
                break;
            case REJECTED:
                sendKycRejectedNotification(verification, "Additional information required");
                break;
            case EXPIRED:
                log.info("KYC verification expired: {}", verification.getVerificationId());
                // Could add expired notification in future
                break;
            default:
                log.warn("Unhandled status change: {}", newStatus);
        }
    }

    /**
     * Send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Send simple text email (fallback)
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Sent simple email to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email", e);
        }
    }

    /**
     * Send reminder email for incomplete KYC
     */
    @Async
    public void sendKycReminderNotification(User user, CustomerKycVerification verification, int daysInactive) {
        try {
            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("daysInactive", daysInactive);
            context.setVariable("verificationId", verification.getVerificationId());
            context.setVariable("continueUrl", frontendUrl + "/kyc/workflow?verificationId=" + verification.getVerificationId());
            context.setVariable("progressPercentage", calculateProgress(verification));

            String htmlContent = templateEngine.process("email/kyc-reminder", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "Complete Your KYC Verification - FINCORE",
                    htmlContent
            );

            log.info("Sent KYC reminder notification to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send KYC reminder notification", e);
        }
    }

    /**
     * Calculate verification progress percentage
     */
    private int calculateProgress(CustomerKycVerification verification) {
        int completedSteps = 0;
        int totalSteps = 5;

        // Basic checks
        if (verification.getUser() != null) completedSteps++;
        if (verification.getSumsubApplicantId() != null) completedSteps++;
        
        // Could add more checks based on actual completion status
        
        return (completedSteps * 100) / totalSteps;
    }
}
