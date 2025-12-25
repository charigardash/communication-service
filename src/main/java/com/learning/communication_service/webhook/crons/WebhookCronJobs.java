package com.learning.communication_service.webhook.crons;

import com.learning.communication_service.dbEntity.webhook.Webhook;
import com.learning.communication_service.dbEntity.webhook.WebhookDelivery;
import com.learning.communication_service.webhook.enums.WebhookStatus;
import com.learning.communication_service.webhook.repository.WebhookDeliveryRepository;
import com.learning.communication_service.webhook.repository.WebhookRepository;
import com.learning.communication_service.webhook.service.WebhookDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookCronJobs {

    private final WebhookDeliveryRepository webhookDeliveryRepository;

    private final WebhookDeliveryService deliveryService;

    private final WebhookRepository webhookRepository;

    @Scheduled(fixedDelay = 60000) // run every minute
    @Transactional
    public void processPendingRetries() {

        LocalDateTime now = LocalDateTime.now();

        List<WebhookDelivery> pendingRetries = webhookDeliveryRepository.findByStatusAndNextRetryAtBefore(WebhookStatus.RETRYING, now);

        pendingRetries.forEach(delivery -> {
            Optional<Webhook> webhookOpt = webhookRepository.findById(delivery.getWebhookId());
            webhookOpt.ifPresent(webhook -> {
                if(webhook.isActive()){
                    deliveryService.deliverToSingleWebhook(webhook, delivery.getEventId(), delivery.getEventType(), delivery.getPayload());
                }
            });
        });

        if (!pendingRetries.isEmpty()) {
            log.debug("Processed {} pending retries", pendingRetries.size());
        }

    }
}
