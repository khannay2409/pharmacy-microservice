package com.org.pharmacy.Events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderPlacedEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private Instant timestamp;
    private List<MedicinePriceDTO> medicinePriceDTOList;
}
