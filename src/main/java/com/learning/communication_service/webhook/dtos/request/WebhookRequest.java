package com.learning.communication_service.webhook.dtos.request;

import com.learning.communication_service.webhook.enums.RetryStrategy;
import com.learning.communication_service.webhook.enums.WebhookEventType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public class WebhookRequest {

    @NotBlank(message = "Webhook name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Target URL is required")
    @Pattern(regexp = "^(https?|ftp)://[^\\\\s/$.?#].[^\\\\s]*$", message = "Invalid URL format")
    private String targetUrl;

    @NotNull(message = "Events cannot be null")
    @Size(min = 1, message = "At least one event must be selected")
    private Set<WebhookEventType> subscribedEvents;

    private String signingSecret;

    private Map<String, String> customHeaders = new HashMap<>();

    private boolean active = true;

    //TODO
    @Min(value = 5, message = "Timeout must be at least 5 seconds")
    @Max(value = 120, message = "Timeout cannot exceed 120 seconds")
    private int timeoutSeconds = 30;

    @Min(value = 0, message = "Retry count cannot be negative")
    @Max(value = 10, message = "Retry count cannot exceed 10")
    private int retryCount = 3;

    private RetryStrategy retryStrategy = RetryStrategy.EXPONENTIAL_BACKOFF;

    private String userId;
}
