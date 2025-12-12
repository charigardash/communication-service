package com.learning.communication_service.webhook.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.webhook")
@Data
public class WebhookConfig {

    private int maxConnections = 100;
    private int maxConnectionsPerRoute = 50;
    private Duration connectionTimeout = Duration.ofSeconds(10);
    private Duration readTimeout = Duration.ofSeconds(30);
    private Duration writeTimeout = Duration.ofSeconds(30);
    private boolean enableRetry = true;
    private int maxRetryAttempts = 3;
    private Duration retryDelay = Duration.ofSeconds(1);
    private boolean enableCircuitBraker = true;
    private int circuitBrakerThreshold = 5;
    private Duration circuitBrakerTimeout = Duration.ofMinutes(5);
    private Map<String, String> defaultHeaders;

    @Bean //TODO
    public WebClient webhookWebClient(){

        //The ConnectionProvider is the core component that manages the pool of TCP connections used by the HttpClient
        ConnectionProvider connectionProvider = ConnectionProvider.builder("webhook")
                .maxConnections(maxConnections) // Total number of active connections allowed
                .maxIdleTime(Duration.ofMinutes(5)) // Connections are closed if unused for this long
                .maxLifeTime(Duration.ofMinutes(10)) // Connections are closed regardless of activity after this time
                .pendingAcquireTimeout(Duration.ofSeconds(30)) // Time a request waits if maxConnections is reached
                .evictInBackground(Duration.ofSeconds(30))  // Frequency of checking and evicting expired/idle connections
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(readTimeout) ;//Sets the maximum time allowed for the entire request-response cycle to complete. If the remote webhook server doesn't respond fully within this readTimeout, the request will fail.
                /**
                 * This enables "wiretapping," which logs the raw request and response data (headers, bodies, and network traffic)
                 * to the console. It should generally be disabled in production as it generates a large amount of logs and can
                 * expose sensitive data.
                 */
//                .wiretap(true); // Enable for debugging

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("User-Agent", "Communication-Webhook/1.0")
                .build();
    }
}
