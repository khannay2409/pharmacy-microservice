package com.org.pharmacy.Gateways;

import com.org.pharmacy.Events.OrderPlacedEvent;
import com.org.pharmacy.Service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class paymentListner {

    @Autowired
    private PaymentService paymentService;

    @KafkaListener(topics = "orders.events", groupId = "payment-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumerOrdersEvent(OrderPlacedEvent event, @Header(name = "X-Trace-Id", required = false) String traceId){

        if (traceId != null && !traceId.isEmpty()) {
            MDC.put("traceId", traceId);
        }
        log.info("Received orders event: {} ", event);
        try {
            paymentService.onOrderPlaced(event);
        } catch (Exception ex) {
            log.error("Failed to process orders event for orderId={} → Sending to DLQ", event.getOrderId(), ex);
            throw ex;
        }
        finally {
            MDC.clear();
        }
    }
}
