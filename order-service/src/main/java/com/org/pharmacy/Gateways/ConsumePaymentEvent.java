package com.org.pharmacy.Gateways;

import com.org.pharmacy.Events.PaymentEvent;
import com.org.pharmacy.Service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsumePaymentEvent {

    @Autowired
    private OrderService orderService;

    @KafkaListener(topics = {"payments.success", "payments.failed"}, groupId = "order-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentEvent(PaymentEvent event) {
        // handle both success and failure
        log.info("Recieved Payment Event: {}", event);
        try {
            orderService.updateStatus(event.getOrderId() ,event.getStatus());
        } catch (Exception ex) {
            log.error("Failed to process PaymentEvent for orderId={} → Sending to DLQ", event.getOrderId(), ex);
            throw ex;
        }
    }

}
