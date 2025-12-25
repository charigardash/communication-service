package com.learning.communication_service.service.impl;

import com.learning.communication_service.enums.OTPType;
import com.learning.communication_service.service.OTPWebhookIntegrationService;
import com.learning.communication_service.webhook.dtos.response.WebhookEvent;
import com.learning.communication_service.webhook.enums.WebhookEventType;
import com.learning.communication_service.webhook.service.WebhookDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPWebhookIntegrationServiceImpl implements OTPWebhookIntegrationService {

    private final WebhookDeliveryService webhookDeliveryService;

    @Async
    @Override
    public void triggerOtpSentEvent(String identifier, OTPType otpType, String otp, String maskedOtp){
        try{
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("identifier", identifier);
            eventData.put("type", otpType.name());
            eventData.put("otp", maskedOtp);
            eventData.put("timestamp", System.currentTimeMillis());
            eventData.put("service", "OTP Authentication");

            WebhookEvent<Map<String, Object>> event = new WebhookEvent<>(WebhookEventType.OTP_SENT, eventData);

            webhookDeliveryService.deliverWebhook(event.getEventId(), event.getEventType(), event);
            log.debug("Triggered OTP_SENT webhook for: {}", identifier);
        } catch (Exception e) {
            log.error("Failed to trigger OTP_SENT webhook for: {}", identifier, e);
        }

    }

    @Async
    @Override
    public void triggerOtpVerifiedEvent(String identifier, OTPType otpType, boolean success, String reason){

        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("identifier", identifier);
            eventData.put("type", otpType);
            eventData.put("success", success);
            eventData.put("reason", reason);
            WebhookEventType webhookEventType = success ? WebhookEventType.OTP_VERIFIED : WebhookEventType.OTP_FAILED;
            WebhookEvent<Map<String, Object>> event = new WebhookEvent<>(webhookEventType, eventData);
            webhookDeliveryService.deliverWebhook(event.getEventId(), event.getEventType(), event);
            log.debug("Triggered {} webhook for: {}", webhookEventType, identifier);
        } catch (Exception e) {
            log.error("Failed to trigger OTP verification webhook for: {}", identifier, e);
        }
    }


    @Async
    @Override
    public void triggerRateLimitExceedEvent(String identifier, String clientIp, String endpoint, int attempts){
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("identifier", identifier);
            eventData.put("clientIp", clientIp);
            eventData.put("endPoint", endpoint);
            eventData.put("attempts", attempts);
            eventData.put("timestamp", System.currentTimeMillis());
            eventData.put("action", "rate_limited");
            WebhookEvent<Map<String, Object>> event = new WebhookEvent<>(WebhookEventType.RATE_LIMIT_EXCEEDED, eventData);
            webhookDeliveryService.deliverWebhook(event.getEventId(), event.getEventType(), event);
            log.debug("Triggered RATE_LIMIT_EXCEEDED webhook for: {}", identifier);

        } catch (Exception e) {
            log.error("Failed to trigger rate limit webhook for: {}", identifier, e);
        }
    }

    @Async
    @Override
    public void triggerSuspiciousActivityEvent(String identifier, String activity, String details){
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("identifier", identifier);
            eventData.put("activity", activity);
            eventData.put("details", details);
            eventData.put("timestamp", System.currentTimeMillis());
            eventData.put("severity", "HIGH");
            WebhookEvent<Map<String, Object>> event = new WebhookEvent<>(WebhookEventType.SUSPICIOUS_ACTIVITY, eventData);
            webhookDeliveryService.deliverWebhook(event.getEventId(), event.getEventType(), event);
            log.debug("Triggered SUSPICIOUS_ACTIVITY webhook for: {}", identifier);
        } catch (Exception e){
            log.error("Failed to trigger suspicious activity webhook for: {}", identifier, e);
        }
    }
}
