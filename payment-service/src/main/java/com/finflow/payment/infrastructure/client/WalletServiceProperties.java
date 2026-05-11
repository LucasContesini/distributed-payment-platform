package com.finflow.payment.infrastructure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "clients.wallet-service")
public record WalletServiceProperties(
        String url,
        Duration connectTimeout,
        Duration readTimeout
) {}
