package com.learning.communication_service.webhook.repository;

import com.learning.communication_service.dbEntity.webhook.WebhookDelivery;
import com.learning.communication_service.webhook.enums.WebhookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface WebhookDeliveryRepository extends MongoRepository<WebhookDelivery, String> {

    Page<WebhookDelivery> findByWebhookId(String webhookId, Pageable pageable);

    List<WebhookDelivery> findByEventId(String eventId);

    List<WebhookDelivery> findByStatusAndNextRetryAtBefore(WebhookStatus status, LocalDateTime time);

    List<WebhookDelivery> findByStatus(WebhookStatus status);

    @Query("{ 'createdAt' : { $gte : ?0 , $lte : ?1}}")
    Page<WebhookDelivery> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("{ 'webhookId': ?0, 'status': ?1 }")
    Page<WebhookDelivery> findByWebhookIdAndStatus(String webhookId, WebhookStatus status, Pageable pageable);

    //TODO
    @Query(value = "{ 'webhookId' : ?0}", count = true)
    long countByWebhookId(String id);

    @Query(value = "{ 'webhookId' : ?0, 'status' : 'SENT'}", count = true)
    long countSuccessfulDeliveries(String webhookId);

    @Query(value = "{ 'webhookId': ?0, 'status': { $in: ['FAILED', 'RETRYING']} }", count = true)
    long countFailedDeliveries(String webhookId);

}
