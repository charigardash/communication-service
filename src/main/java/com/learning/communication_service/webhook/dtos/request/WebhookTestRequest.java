package com.learning.communication_service.webhook.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class WebhookTestRequest {

    @NotBlank(message = "Target URL is required")
    private String targetUrl;

    private String signingSecret;

    private Map<String, String> customHeaders = new HashMap<>();

    private String payload = "{\"test\": true, \"message\": \"Webhook test payload\"}";
}
