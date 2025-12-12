package com.learning.communication_service.webhook.repository;

import com.learning.communication_service.dbEntity.webhook.Webhook;
import com.learning.communication_service.webhook.enums.WebhookEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookRepository extends MongoRepository<Webhook, String> {

    List<Webhook> findByActiveTrue();

    List<Webhook> findByUserIdAndActiveTrue(String userId);

    @Query("{ 'active'  : true, 'subscribedEvents' : ?0 }")
    List<Webhook> findActiveByEventType(WebhookEventType eventType);

    @Query("{'active':true, 'subscribedEvents' : { $in: ?0}}")
    List<Webhook> findActiveByEventTypes(List<WebhookEventType> eventTypes);

    @Query("{ 'active' : true, 'targetUrl' : ?0}")
    Optional<Webhook> findUrlAndActive(String url);

    @Query("{ 'createdAt' : { $gte : ?0 $lte : ?1} }")
    Page<Webhook> findCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    boolean existsByTargetUrlAndActiveTrue(String targetUrl);

}
