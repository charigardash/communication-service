package com.learning.communication_service.webhook.enums;

public enum RetryStrategy {

    NONE,   // No retry
    IMMEDIATE,  // Immediate retry
    LINEAR_BACKOFF,   // Fixed delay between retries
    EXPONENTIAL_BACKOFF, // Exponential delay (2^n seconds)
    FIBONACCI_BACKOFF  // Fibonacci sequence delay
}
