package com.org.pharmacy.Events;

import com.org.pharmacy.Enum.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class PaymentEvent {
    private Long orderId;
    private Long paymentId;
    private List<MedicinePriceDTO> medicinePriceDTOList;
    private PaymentStatus status; // COMPLETED / FAILED
    private BigDecimal amount;
    private Instant timestamp;
}
