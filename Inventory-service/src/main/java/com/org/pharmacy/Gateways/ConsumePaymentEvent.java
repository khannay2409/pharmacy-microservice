package com.org.pharmacy.Gateways;

import com.org.pharmacy.Events.PaymentEvent;
import com.org.pharmacy.Service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsumePaymentEvent {

    @Autowired
    private InventoryService inventoryService;

    @KafkaListener(topics = {"payments.success"}, groupId = "inventory-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentEvent(PaymentEvent event, @Header(name = "X-Trace-Id", required = false) String traceId) {
        // handle both success and failure
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put("traceId", traceId);
        }
        log.info("Recieved Payment Event: {}", event);
        try {
            inventoryService.updateStock(event.getOrderId(), event.getMedicinePriceDTOList());
        } catch (Exception ex) {
            log.error("Failed to process PaymentEvent for orderId={} → Sending to DLQ", event.getOrderId(), ex);
            throw ex;
        }
        finally {
            MDC.clear();
        }
    }
}
