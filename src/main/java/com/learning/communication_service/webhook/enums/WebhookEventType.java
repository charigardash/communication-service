package com.learning.communication_service.webhook.enums;

public enum WebhookEventType {

    // OTP Events
    OTP_SENT,
    OTP_VERIFIED,
    OTP_EXPIRED,
    OTP_FAILED,

    // User Events

    USER_REGISTERED,
    USER_LOGIN,
    USER_LOGOUT,
    USER_PASSWORD_CHANGED,

    // Security Events
    RATE_LIMIT_EXCEEDED,
    SUSPICIOUS_ACTIVITY,
    LOGIN_ATTEMPT_FAILED,


    // System Events
    SYSTEM_HEALTH_CHECK,
    API_USAGE_EXCEEDED,

    // Custom Events (can be extended)
    CUSTOM_EVENT;

    public static WebhookEventType fromString(String value){
        try {
            return WebhookEventType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return CUSTOM_EVENT;
        }
    }
}
