package com.learning.communication_service.webhook.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.communication_service.dbEntity.webhook.Webhook;
import com.learning.communication_service.dbEntity.webhook.WebhookDelivery;
import com.learning.communication_service.webhook.dtos.CircuitBreakerState;
import com.learning.communication_service.webhook.enums.RetryStrategy;
import com.learning.communication_service.webhook.enums.WebhookEventType;
import com.learning.communication_service.webhook.enums.WebhookStatus;
import com.learning.communication_service.webhook.exception.WebhookDeliveryException;
import com.learning.communication_service.webhook.repository.WebhookDeliveryRepository;
import com.learning.communication_service.webhook.repository.WebhookRepository;
import com.learning.communication_service.webhook.service.WebhookDeliveryService;
import com.learning.communication_service.webhook.service.WebhookSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryServiceImpl implements WebhookDeliveryService {

    private final WebhookDeliveryRepository deliveryRepository;

    private final WebhookSecurityService securityService;

    private final WebClient webClient;

    private final WebhookRepository webhookRepository;

    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, CircuitBreakerState> circuitBreakerStates = new ConcurrentHashMap<>();

    @Async("webhookTaskExecutor")
    @Transactional
    @Override
    public CompletableFuture<Void> deliverWebhook(String eventId, WebhookEventType eventType, Object payload){
        try{
            String payloadJson = objectMapper.writeValueAsString(payload);

            // Find active webhooks subscribed to this event
            List<Webhook> subscribedWebhooks = webhookRepository.findActiveByEventType(eventType);
            if(subscribedWebhooks.isEmpty()){
                log.debug("No webhooks subscribed to event: {}", eventType);
                return CompletableFuture.completedFuture(null);
            }
            log.info("Delivering event {} to {} webhooks", eventType, subscribedWebhooks.size());

            // Deliver to each webhook asynchronously
            List<CompletableFuture<WebhookDelivery>> futures = subscribedWebhooks.stream()
                    .filter(webhook -> !isCircuitBreakerOpen(webhook.getId()))
                    .map(webhook -> deliverToSingleWebhook(webhook, eventId, eventType, payloadJson))
                    .toList();

            // Wait for all deliveries to complete
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        } catch (Exception e) {
            log.error("Failed to deliver webhook for event: {}", eventType, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @Async("webhookTaskExecutor")
    public CompletableFuture<WebhookDelivery> deliverToSingleWebhook(Webhook webhook, String eventId, WebhookEventType webhookEventType, String payloadJson) {

        return CompletableFuture.supplyAsync(()->{
            WebhookDelivery delivery = createDeliveryRecord(webhook, eventId, webhookEventType, payloadJson);
            try {
                deliverWithRetry(webhook, delivery);
                webhook.incrementSuccessCount();
                log.info("Webhook delivered successfully: {} -> {}", webhook.getName(), webhook.getTargetUrl());
            } catch (Exception e) {
                handleDeliveryFailure(webhook, delivery, e);
                log.error("Failed to deliver webhook: {} -> {}", webhook.getName(), webhook.getTargetUrl(), e);
            }
            // Save updated webhook statistics
            webhookRepository.save(webhook);
            return deliveryRepository.save(delivery);
        });
    }

    @Retryable(
            value = {WebhookDeliveryException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private void deliverWithRetry(Webhook webhook, WebhookDelivery delivery) {

        long startTime = System.currentTimeMillis();

        try {
            Map<String, String> securityHeaders = securityService.generateSecurityHeaders(
                    delivery.getPayload(),
                    webhook.getSigningSecret(),
                    delivery.getEventType().name(),
                    delivery.getId()
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAll(securityHeaders);
            // Add custom headers
            if(webhook.getCustomHeaders() != null)headers.setAll(webhook.getCustomHeaders());
            String responseBody = webClient.post()
                    .uri(webhook.getTargetUrl())
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .bodyValue(delivery.getPayload())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new WebhookDeliveryException( "Webhook delivery failed with status: " + clientResponse.statusCode() +
                                            ", body: " + errorBody)))
                    )
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(webhook.getTimeoutSeconds()))
                    .block();
            long processingTime = System.currentTimeMillis() - startTime;
            delivery.markAsSent(200, responseBody);
            delivery.setProcessingTimeMs(processingTime);
            delivery.setRequestHeaders(convertHeaders(headers));
            delivery.setSentAt(LocalDateTime.now());

            // Reset circuit breaker on success
            resetCircuitBreaker(webhook.getId());
        } catch (WebClientResponseException e){
            handleWebClientError(webhook, delivery, e);
            throw new WebhookDeliveryException("Webhook delivery failed: " + e.getStatusCode(), e);
        } catch (Exception e) {
            delivery.markAsFailed(e.getMessage());
            delivery.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            throw new WebhookDeliveryException("Webhook delivery failed", e);
        }

    }


    private void handleDeliveryFailure(Webhook webhook, WebhookDelivery delivery, Exception e) {
        webhook.incrementFailureCount();
        delivery.markAsFailed(e.getMessage());
        if(delivery.canRetry()){
            scheduleRetry(webhook, delivery, e.getMessage());
        }else {
            incrementCircuitBrakerFailure(webhook.getId());
        }
    }

    private void handleWebClientError(Webhook webhook, WebhookDelivery delivery, WebClientResponseException e) {
        delivery.setResponseCode(e.getStatusCode().value());
        delivery.setResponseBody(e.getResponseBodyAsString());
        if(shouldRetry(e.getStatusCode().value())){
            scheduleRetry(webhook, delivery, e.getMessage());
        }else{
            delivery.markAsFailed(e.getMessage());
            incrementCircuitBrakerFailure(webhook.getId());
        }
    }

    private void incrementCircuitBrakerFailure(String id) {
        CircuitBreakerState circuitBreakerState = circuitBreakerStates.computeIfAbsent(id, k -> new CircuitBreakerState());
        circuitBreakerState.failureCount++;
        circuitBreakerState.lastFailureTime = System.currentTimeMillis();
        if(circuitBreakerState.failureCount >= 5){// Threshold
            circuitBreakerState.openUtil = System.currentTimeMillis() + (5*60*1000); // 5 minutes
            log.warn("Circuit breaker opened for webhook: {}", id);
        }
    }

    private void scheduleRetry(Webhook webhook, WebhookDelivery delivery, String message) {
        LocalDateTime nextRetry = calculateNextRetryTime(delivery.getAttemptCount(), webhook.getRetryStrategy());
        delivery.markAsRetrying(nextRetry);
        delivery.setErrorMessage(message);
        delivery.incrementAttemptCount();
        log.info("Scheduled retry {} for webhook {} at {}",
                delivery.getAttemptCount(), webhook.getName(), nextRetry);
    }

    private LocalDateTime calculateNextRetryTime(int attemptCount, RetryStrategy retryStrategy) {
        long delaySeconds  = 0;
        switch (retryStrategy){
            case IMMEDIATE:
                delaySeconds = 1;
                break;
            case LINEAR_BACKOFF:
                delaySeconds = 5L * attemptCount;
                break;
            case EXPONENTIAL_BACKOFF:
                delaySeconds = (long) Math.pow(2, attemptCount);
                break;
            case FIBONACCI_BACKOFF:
                delaySeconds = fibonacci(attemptCount + 1);
                break;
            default:
                delaySeconds = 5;
        }

        // Cap delay at 5 minutes
        delaySeconds = Math.min(delaySeconds, 300);
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

    private long fibonacci(int n) {
        if(n <= 1)return 1;
        int a = 0, b = 1;
        for(int i=0;i<n;i++){
            int temp = a+b;
            a=b;
            b=temp;
        }
        return b;
    }

    private boolean shouldRetry(int value) {
        // Retry on server errors and 429 (Too Many Requests)
        return value >= 500 || value == 429;
    }

    private void resetCircuitBreaker(String id) {
        CircuitBreakerState circuitBreakerState = circuitBreakerStates.get(id);
        if (circuitBreakerState != null){
            circuitBreakerState.failureCount = 0;
            circuitBreakerState.openUtil = 0;
        }
    }

    private boolean isCircuitBreakerOpen(String webhookId){
        CircuitBreakerState state = circuitBreakerStates.get(webhookId);
        if(state == null) return false;
        if(state.openUtil > 0 && System.currentTimeMillis() < state.openUtil)return true;
        else if(state.openUtil > 0){
            // Circuit breaker timeout expired
            state.openUtil = 0;
            state.failureCount = 0;
            return false;
        }
        return false;
    }

    private Map<String, String> convertHeaders(HttpHeaders headers){
        Map<String, String> map = new HashMap<>();
        headers.forEach((key, value)->map.put(key, String.join(", ", value)));
        return map;
    }

    private WebhookDelivery createDeliveryRecord(Webhook webhook, String eventId,
                                                 WebhookEventType eventType, String payloadJson) {
        WebhookDelivery delivery = new WebhookDelivery();
        delivery.setWebhookId(webhook.getId());
        delivery.setEventId(eventId);
        delivery.setEventType(eventType);
        delivery.setPayload(payloadJson);
        delivery.setTargetUrl(webhook.getTargetUrl());
        delivery.setMaxAttempts(webhook.getRetryCount());
        delivery.setStatus(WebhookStatus.PENDING);

        return deliveryRepository.save(delivery);
    }
}
