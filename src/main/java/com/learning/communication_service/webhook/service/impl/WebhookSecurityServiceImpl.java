package com.learning.communication_service.webhook.service.impl;

import com.learning.communication_service.webhook.exception.WebhookSecurityException;
import com.learning.communication_service.webhook.service.WebhookSecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
public class WebhookSecurityServiceImpl implements WebhookSecurityService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SIGNATURE_HEADER = "X-Webhook-Signature";
    private static final String TIMESTAMP_HEADER = "X-Webhook-Timestamp";
    private static final String EVENT_HEADER = "X-Webhook-Event";
    private static final String DELIVERY_HEADER = "X-Webhook-Delivery-Id";

    @Override
    public Map<String, String> generateSecurityHeaders(String payload, String secret, String eventType, String deliveryId){
        Map<String, String> headers = new TreeMap<>();
        long timestamp = System.currentTimeMillis()/1000;
        String signature = generateSignature(payload, secret, timestamp);

        headers.put(SIGNATURE_HEADER, signature);
        headers.put(TIMESTAMP_HEADER, String.valueOf(timestamp));
        headers.put(EVENT_HEADER, eventType);
        headers.put(DELIVERY_HEADER, deliveryId);

        return headers;
    }

    private String generateSignature(String payload, String secret, long timestamp) {

        try {
            String data = String.format("%d.%s", timestamp, payload);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to generate webhook signature", e);
            throw new WebhookSecurityException("Failed to generate signature", e);
        }

    }

    public boolean verifySignature(String payload, String secret, String signature,
                                   long timestamp, long toleranceSeconds){
        // Check timestamp tolerance
        long currentTimestamp = System.currentTimeMillis();
        if(Math.abs(currentTimestamp-timestamp) > toleranceSeconds)return false;

        // Generate expected signature
        String expectedSignature = generateSignature(payload, secret, currentTimestamp);
        // Constant-time comparison to prevent timing attacks
        return constantTimeEquals(expectedSignature, signature);
    }

    private boolean constantTimeEquals(String expectedSignature, String signature) {
        if(expectedSignature.length() != signature.length())return false;
        int result = 0;
        for(int i=0;i<expectedSignature.length();i++){
            result+=(expectedSignature.charAt(i)^signature.charAt(i));
        }
        return result == 0;
    }
}
