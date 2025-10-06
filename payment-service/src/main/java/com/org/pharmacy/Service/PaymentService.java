package com.org.pharmacy.Service;

import com.org.pharmacy.Entity.Payment;
import com.org.pharmacy.Enum.PaymentStatus;
import com.org.pharmacy.Events.OrderPlacedEvent;
import com.org.pharmacy.Events.PaymentEvent;
import com.org.pharmacy.Repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Random;

@Slf4j
@Component
public class PaymentService {

    @Autowired
    private  PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

    private final Random rnd = new Random();

    @Transactional
    public void onOrderPlaced(OrderPlacedEvent event) {
        // idempotency check
        if (paymentRepository.existsByOrderId(event.getOrderId())) return;

        // simulate payment processing
        boolean success = rnd.nextInt(100) < 80; // 80% success
        Payment p = new Payment();
        p.setOrderId(event.getOrderId());
        p.setAmount(event.getTotalAmount());
        p.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        p = paymentRepository.save(p);

        // publish PaymentEvent
        PaymentEvent pe = new PaymentEvent(event.getOrderId(),  p.getId(), event.getMedicinePriceDTOList(), p.getStatus(), p.getAmount(), Instant.now());
        log.info("publishing payment event:{}",pe);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String traceId = MDC.get("traceId");
                if(pe.getStatus().equals(PaymentStatus.SUCCESS)) {
                    ProducerRecord<String, Object> record = new ProducerRecord<>("payments.success", pe);
                    record.headers().add("X-Trace-Id", traceId.getBytes());
                    kafkaTemplate.send(record);
                } else {
                    ProducerRecord<String, Object> record = new ProducerRecord<>("payments.failed", pe);
                    record.headers().add("X-Trace-Id", traceId.getBytes());
                    kafkaTemplate.send(record);
                }
            }
        });
    }

}
