package com.learning.communication_service.webhook.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebhookStatistics {
    private long totalDeliveries;
    private long successfulDeliveries;
    private long failedDeliveries;
    private double successRate;
    private long lastDeliveryTime;
}
