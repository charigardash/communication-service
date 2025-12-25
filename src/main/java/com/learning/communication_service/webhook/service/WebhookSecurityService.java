package com.learning.communication_service.webhook.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface WebhookSecurityService {
    Map<String, String> generateSecurityHeaders(String payload, String secret, String eventType, String deliveryId);
}
