package com.org.pharmacy.Gateways;

import com.org.pharmacy.Events.StockAvailableRequest;
import com.org.pharmacy.Events.StockAvailableResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.Map;

@Component
public class InventoryClient {

    private final WebClient webClient;

    public InventoryClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://inventory-service").build();
    }

    @Retryable(
            value = {WebClientRequestException.class},
            maxAttempts = 4,
            backoff = @Backoff(delay = 1000, multiplier = 2) // 1s, 2s, 4s, 8s
    )
    public StockAvailableResponse checkStock(StockAvailableRequest request) {
        return (webClient.post()
                .uri("/inventory/checkStock")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(StockAvailableResponse.class)
                .block());
    }

}
