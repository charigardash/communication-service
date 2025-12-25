package com.learning.communication_service.webhook.controller;

import com.learning.communication_service.webhook.dtos.request.WebhookRequest;
import com.learning.communication_service.webhook.dtos.request.WebhookUpdateRequest;
import com.learning.communication_service.webhook.dtos.response.PaginatedResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookDeliveryResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookStatistics;
import com.learning.communication_service.webhook.enums.WebhookStatus;
import com.learning.communication_service.webhook.service.WebhookManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/communication/webhooks/")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Webhook management and delivery apis")
public class WebhookController {

    private final WebhookManagementService webhookManagementService;

    @PostMapping
    @Operation(summary = "Create a new webhook")
    public ResponseEntity<?> createWebhook(@Valid @RequestBody WebhookRequest webhookRequest){
        WebhookResponse webhookResponse = webhookManagementService.createWebhook(webhookRequest);
        return ResponseEntity.status(CREATED).body(webhookResponse);

    }

    @GetMapping("/{id}")
    @Operation(summary = "Get webhook by ID")
    public ResponseEntity<?> getWebhook(@Parameter(description = "Webhook Id") @PathVariable String id){
        WebhookResponse response = webhookManagementService.getWebhook(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all webhooks with pagination")
    public ResponseEntity<?> getAllWebhooks(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int pageSize
            ){
        PaginatedResponse<WebhookResponse> response = webhookManagementService.getAllWebhooks(page, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get webhooks by user ID")
    public ResponseEntity<?> getWebhookByUser(
            @Parameter(description = "User ID") @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize){
        PaginatedResponse<WebhookResponse> webhookByUser = webhookManagementService.getWebhookByUser(userId, page, pageSize);
        return ResponseEntity.ok(webhookByUser);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update webhook")
    public ResponseEntity<?> updateWebhook(@Parameter(description = "webhook id") @PathVariable String webhookId,
                                           @Valid @RequestBody WebhookUpdateRequest webhookUpdateRequest){
        WebhookResponse webhookResponse = webhookManagementService.updateWebhook(webhookId, webhookUpdateRequest);
        return ResponseEntity.ok(webhookResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete webhook")
    public ResponseEntity<?> deleteWebhook(@Parameter(description = "webhook id") @PathVariable String webhookId){
        webhookManagementService.deleteWebhook(webhookId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "Disable webhook")
    public ResponseEntity<?> disableWebhook(@Parameter(description = "Webhook ID") @PathVariable String id){
        webhookManagementService.disableWebhook(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "Enable webhook")
    public ResponseEntity<Void> enableWebhook(
            @Parameter(description = "Webhook ID") @PathVariable String id) {
        webhookManagementService.enableWebhook(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Trigger test webhook")
    public ResponseEntity<?> testWebhook(
            @Parameter(description = "Webhook ID") @PathVariable String id,
            @RequestBody(required = false) Object testPayload
    ){
        Object payload = testPayload != null ? testPayload :
                new TestPayload("Test webhook delivery", "success", System.currentTimeMillis());
        webhookManagementService.triggerTestWebhook(id, testPayload);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}/deliveries")
    @Operation(summary = "Get webhook delivery history")
    public ResponseEntity<?> getDeliveries(@Parameter(description = "Webhook ID") @PathVariable String id,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "0") int pageNumber,
                                           @RequestParam(defaultValue = "20") int pageSize){
        PaginatedResponse<WebhookDeliveryResponse> response = webhookManagementService.getWebhookDeliveries(id, status != null? WebhookStatus.valueOf(status):null,
                pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry failed deliveries")
    public ResponseEntity<?> retryFailedDeliveries( @Parameter(description = "Webhook ID") @PathVariable String id){
        webhookManagementService.retryFailedDeliveries(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "Get webhook statistics")
    public ResponseEntity<?> getStatistics(@Parameter(description = "Webhook Id") @PathVariable String id){
        WebhookStatistics webhookStatistics = webhookManagementService.getStatistics(id);
        return ResponseEntity.ok(webhookStatistics);
    }

    private record TestPayload(String message, String status, long timestamp){};
}
