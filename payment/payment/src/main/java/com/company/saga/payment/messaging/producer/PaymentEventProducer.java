package com.company.saga.payment.messaging.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentSuccess(UUID orderId) {
        kafkaTemplate.send("payment-events", orderId + ":SUCCESS");
    }

    public void sendPaymentFailure(UUID orderId) {
        kafkaTemplate.send("payment-events", orderId + ":FAILED");
    }
}
