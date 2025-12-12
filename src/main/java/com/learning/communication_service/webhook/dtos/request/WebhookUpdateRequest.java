package com.learning.communication_service.webhook.dtos.request;

import com.learning.communication_service.webhook.enums.RetryStrategy;
import com.learning.communication_service.webhook.enums.WebhookEventType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public class WebhookUpdateRequest {

    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
            message = "Invalid URL format")
    private String targetUrl;

    @Size(min = 1, message = "At least one event must be selected")
    private Set<WebhookEventType> subscribedEvents;

    private String signingSecret;

    private Map<String, String> customHeaders;

    private Boolean active;

    @Min(value = 5, message = "Timeout must be at least 5 seconds")
    @Max(value = 120, message = "Timeout cannot exceed 120 seconds")
    private Integer timeoutSeconds;

    @Min(value = 0, message = "Retry count cannot be negative")
    @Max(value = 10, message = "Retry count cannot exceed 10")
    private Integer retryCount;

    private RetryStrategy retryStrategy;

}
