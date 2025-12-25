# 🔐 OTP Authentication & Communication Service

Secure OTP delivery, real-time notifications, and enterprise webhooks — a production-ready, scalable authentication and communication system.

## Table of contents

- Overview
- Features
- Architecture
- Quick start
- Configuration
- API reference
- Contributing
- License

## Overview

This service provides one-time password (OTP) authentication and a flexible communication platform for delivering OTPs and security events to end users and external systems. It is designed for production use with horizontal scalability, observability, and robust security controls.

## Features

### Core authentication
- Dual-channel OTP delivery: Email and SMS, simultaneously or independently.
- Configurable expiration and single-use enforcement for OTPs.
- Verification tracking and audit logs for all OTP attempts.

### Advanced security
- Intelligent rate limiting (Token Bucket, Sliding Window, Fixed Window strategies).
- Redis-backed distributed limits and caching.
- Circuit breaker patterns for external integrations.
- HMAC-signed webhooks for secure event delivery.

### Webhook integration
- Event-driven architecture with multiple OTP and security event types.
- Configurable retry strategies (exponential, linear, Fibonacci backoff).
- At-least-once delivery semantics with delivery tracking and dead-letter handling.
- Real-time monitoring of webhook throughput and health.

### Observability
- Delivery analytics (success rates, latency, failure analysis).
- Structured logging and tracing for debugging and compliance.
- Health endpoints and metrics for integration with monitoring systems.

### Scalability & reliability
- Asynchronous, non-blocking processing for delivery pipelines.
- Stateless service design to enable horizontal scaling.
- Connection pooling and batch processing for efficiency.

## Architecture

High-level components:

- API Gateway / Load Balancer
- OTP Authentication Service (REST API)
  - OTP generation & validation
  - Rate limiting & security
  - Webhook management
- Business logic layer (OTP, Rate Limiter, Webhook, Security)
- Async processing layer (email, SMS, webhook delivery queues)
- Persistence & infrastructure: MongoDB, Redis, external Email/SMS providers

The following ASCII diagram illustrates the high-level flow:

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Applications                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │  Web App │  │ Mobile   │  │  API     │  │  CLI     │    │
│  │          │  │  App     │  │ Clients  │  │ Tools    │    │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘    │
│       │              │              │              │         │
└───────┼──────────────┼──────────────┼──────────────┼─────────┘
│              │              │              │
▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────┐
│                API Gateway / Load Balancer                   │
└──────────────────────────────┬──────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│               OTP Authentication Service                     │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                     REST API Layer                    │   │
│  │  • OTP Generation & Validation                       │   │
│  │  • Rate Limiting & Security                          │   │
│  │  • Webhook Management                                │   │
│  └──────────────┬───────────────────────────────────────┘   │
│                 │                                           │
│  ┌──────────────┴───────────────────────────────────────┐   │
│  │                Business Logic Layer                    │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │   │
│  │  │  OTP     │ │  Rate    │ │ Webhook  │ │ Security │ │   │
│  │  │ Service  │ │ Limiting │ │ Service  │ │ Service  │ │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ │   │
│  └──────────────┬───────────────────────────────────────┘   │
│                 │                                           │
│  ┌──────────────┴───────────────────────────────────────┐   │
│  │               Async Processing Layer                   │   │
│  │  • Email Delivery Queue                              │   │
│  │  • SMS Delivery Queue                                │   │
│  │  • Webhook Delivery Queue                            │   │
│  └──────────────┬───────────────────────────────────────┘   │
└─────────────────┼───────────────────────────────────────────┘
│
┌─────────────┼─────────────┐
▼             ▼             ▼
┌─────────┐ ┌─────────┐ ┌─────────────┐
│ MongoDB │ │ Redis   │ │   External  │
│         │ │         │ │  Services   │
│ • OTP   │ │ • Rate  │ │ • Email     │
│   Data  │ │  Limits │ │   Providers │
│ • Users │ │ • Cache │ │ • SMS       │
│ • Web-  │ │ •       │ │   Gateways  │
│   hooks │ │  Sessions│ │ • Webhook   │
│         │ │         │ │   Receivers │
└─────────┘ └─────────┘ └─────────────┘
```

## Quick start

Prerequisites:
- Java 11+ (or the Java version your build uses)
- Maven
- MongoDB and Redis running (local or remote)

Run locally (Maven example):

```bash
# set required environment variables (see Configuration below)
export SPRING_PROFILES_ACTIVE=local
mvn clean package
mvn spring-boot:run
```


## Configuration

Configuration is driven by environment variables or configuration files. Typical vars:

- DATABASE_URL / MONGODB_URI
- REDIS_URL
- SMTP_HOST, SMTP_USERNAME, SMTP_PASSWORD
- SMS_GATEWAY_API_KEY
- WEBHOOK_SIGNING_SECRET
- OTP_EXPIRATION_SECONDS

See the project's configuration files for the full list and defaults.

