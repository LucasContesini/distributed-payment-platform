package com.finflow.payment.infrastructure.client;

import com.finflow.payment.domain.exception.InsufficientFundsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class WalletServiceClient {

    private final RestClient restClient;

    public WalletServiceClient(WalletServiceProperties properties, RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl(properties.url())
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                        throw new InsufficientFundsException();
                    }
                    throw new RestClientResponseException(
                            "Wallet service error: " + response.getStatusCode(),
                            response.getStatusCode().value(),
                            response.getStatusText(),
                            response.getHeaders(),
                            null, null);
                })
                .build();
    }

    public void reserve(UUID paymentId, UUID userId, BigDecimal amount, String currency) {
        restClient.post()
                .uri("/internal/wallets/reserve")
                .body(new ReserveRequest(paymentId, userId, amount, currency))
                .retrieve()
                .toBodilessEntity();
    }

    public void settle(UUID paymentId, UUID userId, BigDecimal amount) {
        restClient.post()
                .uri("/internal/wallets/settle")
                .body(new SettleRequest(paymentId, userId, amount))
                .retrieve()
                .toBodilessEntity();
    }

    public void release(UUID paymentId, UUID userId, BigDecimal amount) {
        restClient.post()
                .uri("/internal/wallets/release")
                .body(new ReleaseRequest(paymentId, userId, amount))
                .retrieve()
                .toBodilessEntity();
    }

    record ReserveRequest(UUID paymentId, UUID userId, BigDecimal amount, String currency) {}
    record SettleRequest(UUID paymentId, UUID userId, BigDecimal amount) {}
    record ReleaseRequest(UUID paymentId, UUID userId, BigDecimal amount) {}
}
