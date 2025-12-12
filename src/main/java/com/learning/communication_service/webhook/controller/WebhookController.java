package com.learning.communication_service.webhook.controller;

import com.learning.communication_service.webhook.dtos.request.WebhookRequest;
import com.learning.communication_service.webhook.dtos.response.PaginatedResponse;
import com.learning.communication_service.webhook.dtos.response.WebhookResponse;
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
}
