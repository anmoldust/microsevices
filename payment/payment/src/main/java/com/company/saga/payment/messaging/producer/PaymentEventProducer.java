package com.company.saga.payment.messaging.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentSuccess(UUID orderId) {
        sendWithEventId("payment-events", orderId + ":SUCCESS");
    }

    public void sendPaymentFailure(UUID orderId) {
        log.info("Payment failed for orderId: {}", orderId);
        sendWithEventId("payment-events", orderId + ":FAILED");
    }

    private void sendWithEventId(String topic, String payload) {
        String eventId = UUID.randomUUID().toString();
        var record = new org.apache.kafka.clients.producer.ProducerRecord<String, String>(topic, payload);
        record.headers().add("eventId", eventId.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(record);
    }
}
