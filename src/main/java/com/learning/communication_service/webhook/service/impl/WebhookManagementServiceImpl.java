package com.learning.communication_service.webhook.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.communication_service.dbEntity.webhook.Webhook;
import com.learning.communication_service.dbEntity.webhook.WebhookDelivery;
import com.learning.communication_service.webhook.dtos.request.WebhookRequest;
import com.learning.communication_service.webhook.dtos.request.WebhookUpdateRequest;
import com.learning.communication_service.webhook.dtos.response.PaginatedResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookDeliveryResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookStatistics;
import com.learning.communication_service.webhook.enums.WebhookEventType;
import com.learning.communication_service.webhook.enums.WebhookStatus;
import com.learning.communication_service.webhook.exception.WebhookException;
import com.learning.communication_service.webhook.repository.WebhookDeliveryRepository;
import com.learning.communication_service.webhook.repository.WebhookRepository;
import com.learning.communication_service.webhook.service.WebhookDeliveryService;
import com.learning.communication_service.webhook.service.WebhookManagementService;
import com.learning.communication_service.webhook.util.WebhookUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.learning.communication_service.webhook.util.ValidationUtil.validateWebhookRequest;
import static com.learning.communication_service.webhook.util.WebhookUtil.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookManagementServiceImpl implements WebhookManagementService {

    private final WebhookRepository webhookRepository;

    private final ObjectMapper objectMapper;

    private final WebhookDeliveryService webhookDeliveryService;

    private final WebhookDeliveryRepository webhookDeliveryRepository;

    @Transactional
    @Override
    public WebhookResponse createWebhook(WebhookRequest webhookRequest) {
        validateWebhookRequest(webhookRequest);
        if(webhookRepository.existsByTargetUrlAndActiveTrue(webhookRequest.getTargetUrl())){
            throw new WebhookException("Active webhook already exists for URL: " +webhookRequest.getTargetUrl(), HttpStatus.CONFLICT);
        }

        Webhook webhook = getWebhookFromRequest(webhookRequest);

        Webhook savedWebhook = webhookRepository.save(webhook);

        log.info("Created webhook: {} -> {}", savedWebhook.getName(), savedWebhook.getTargetUrl());

        return convertToResponse(savedWebhook);
    }

    @Transactional(readOnly = true)
    @Override
    public WebhookResponse getWebhook(String id) {
        Webhook webhook =  webhookRepository.findById(id).orElseThrow(() ->new WebhookException("Webhook not found: " + id,
                HttpStatus.NOT_FOUND));

        return convertToResponse(webhook);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<WebhookResponse> getAllWebhooks(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
        Page<Webhook> webhooks = webhookRepository.findAll(pageable);
        return WebhookUtil.createPaginatedResponse(webhooks);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<WebhookResponse> getWebhookByUser(String userId, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Webhook> webhooks = webhookRepository.findByUserIdAndActiveTrue(userId, pageable);
        return createPaginatedResponse(webhooks);
    }

    @Transactional
    @Override
    public WebhookResponse updateWebhook(String id, WebhookUpdateRequest request) {
        Webhook webhook = webhookRepository.findById(id).orElseThrow(()-> new WebhookException("Webhook not found: "+id, HttpStatus.BAD_REQUEST));
        if(request.getName() != null){
            webhook.setName(request.getName());
        }
        if(request.getTargetUrl() != null){
            if(!webhook.getTargetUrl().equals(request.getTargetUrl())){
                Optional<Webhook> existing = webhookRepository.findByTargetUrlAndActiveTrue(request.getTargetUrl());
                if(existing.isPresent() && existing.get().getId().equals(id)){
                    throw new WebhookException("Another active webhook already exists for URL: "+ request.getTargetUrl(), HttpStatus.CONFLICT);
                }
            }
            webhook.setTargetUrl(request.getTargetUrl());
        }

        if (request.getSubscribedEvents() != null) {
            webhook.setSubscribedEvents(request.getSubscribedEvents());
        }

        if (request.getSigningSecret() != null) {
            webhook.setSigningSecret(request.getSigningSecret());
        }

        if (request.getCustomHeaders() != null) {
            webhook.setCustomHeaders(request.getCustomHeaders());
        }

        if (request.getActive() != null) {
            webhook.setActive(request.getActive());
        }

        if (request.getTimeoutSeconds() != null) {
            webhook.setTimeoutSeconds(request.getTimeoutSeconds());
        }

        if (request.getRetryCount() != null) {
            webhook.setRetryCount(request.getRetryCount());
        }

        if (request.getRetryStrategy() != null) {
            webhook.setRetryStrategy(request.getRetryStrategy());
        }

        webhook.setUpdatedAt(LocalDateTime.now());
        Webhook updated = webhookRepository.save(webhook);

        log.info("Updated webhook: {}", id);
        return convertToResponse(updated);
    }

    @Transactional
    @Override
    public void deleteWebhook(String id){
        Webhook webhook = webhookRepository.findById(id).orElseThrow(()-> new WebhookException("Webhook not found: "+id, HttpStatus.BAD_REQUEST));
        webhookRepository.delete(webhook);
        log.info("Deleted webhook: {}", id);
    }

    @Transactional
    @Override
    public void disableWebhook(String id){
        Webhook webhook = webhookRepository.findById(id).orElseThrow(()-> new WebhookException("Webhook not found: "+id, HttpStatus.BAD_REQUEST));
        webhook.setActive(false);
        webhookRepository.save(webhook);
        log.info("Disabled webhook: {}", id);
    }

    @Transactional
    @Override
    public void enableWebhook(String id){
        Webhook webhook = webhookRepository.findById(id).orElseThrow(()-> new WebhookException("Webhook not found: "+id, HttpStatus.BAD_REQUEST));
        webhook.setActive(true);
        webhookRepository.save(webhook);
        log.info("Enabled webhook: {}", id);
    }

    @Transactional
    @Override
    public void triggerTestWebhook(String id, Object testPayload){
        Webhook webhook = webhookRepository.findById(id).orElseThrow(()-> new WebhookException("Webhook not found: "+id, HttpStatus.BAD_REQUEST));
        if(!webhook.isActive()){
            throw new WebhookException("Webhook is not active", HttpStatus.BAD_REQUEST);
        }

        try {

            String eventId = UUID.randomUUID().toString();
            String payloadJson = objectMapper.writeValueAsString(testPayload);
            webhookDeliveryService.deliverToSingleWebhook(webhook, eventId, WebhookEventType.CUSTOM_EVENT, payloadJson);

        } catch (Exception e) {
            throw new WebhookException("Failed to trigger test webhook: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<WebhookDeliveryResponse> getWebhookDeliveries(String id, WebhookStatus webhookStatus, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("created_at").descending());

        Page<WebhookDelivery> deliveries;

        if(webhookStatus != null){
            deliveries = webhookDeliveryRepository.findByWebhookIdAndStatus(id, webhookStatus, pageable);
        }else {
            deliveries = webhookDeliveryRepository.findByWebhookId(id, pageable);
        }
        List<WebhookDeliveryResponse> content = deliveries.getContent().stream()
                .map(WebhookUtil::convertToDeliveryResponse)
                .toList();
        return createPaginatedResponse(deliveries, content);
    }

    @Transactional
    @Override
    public void retryFailedDeliveries(String id) {
        List<WebhookDelivery> failedDeliveries = webhookDeliveryRepository.findByWebhookIdAndStatus(id, WebhookStatus.FAILED, PageRequest.of(0, 20)).getContent();
        Webhook webhook = webhookRepository.findById(id).orElseThrow(() -> new WebhookException("Webhook not found: " + id, HttpStatus.BAD_REQUEST));
        failedDeliveries.forEach(delivery -> {
            if(delivery.canRetry()){
                webhookDeliveryService.deliverToSingleWebhook(webhook, delivery.getEventId(), delivery.getEventType(), delivery.getPayload());
            }
        });
        log.info("Retried {} failed deliveries for webhook: {}", failedDeliveries.size(), id);
    }

    @Transactional(readOnly = true)
    @Override
    public WebhookStatistics getStatistics(String id) {
        Webhook webhook = webhookRepository.findById(id).orElseThrow(() -> new WebhookException("Webhook not found: " + id, HttpStatus.BAD_REQUEST));
        Webhook.WebhookStatistics statistics = webhook.getStatistics();
        return WebhookStatistics.builder()
                .totalDeliveries(statistics.getTotalAttempts())
                .successfulDeliveries(statistics.getSuccessfulDeliveries())
                .failedDeliveries(statistics.getFailedDeliveries())
                .successRate(statistics.getSuccessRate())
                .lastDeliveryTime(statistics.getLastDeliveryAttempt())
                .build();
    }


}
