package com.learning.communication_service.webhook.dtos;

import org.springframework.stereotype.Component;

@Component
public class CircuitBreakerState {
    public int failureCount;
    public long lastFailureTime;
    public long openUtil;
}
