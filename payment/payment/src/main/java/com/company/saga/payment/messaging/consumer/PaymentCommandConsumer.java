package com.company.saga.payment.messaging.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.company.saga.payment.service.PaymentService;

import java.util.UUID;

@Component
public class PaymentCommandConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentCommandConsumer.class);

    private final PaymentService paymentService;

    public PaymentCommandConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "payment-commands")
    public void consume(String message) {
        log.info("Received PAYMENT command message='{}'", message);

        try {
            UUID orderId = UUID.fromString(message);
            log.info("Processing payment for orderId={}", orderId);

            paymentService.processPayment(orderId);

            log.info("Payment processing completed for orderId={}", orderId);
        } catch (Exception ex) {
            log.error("Failed to process PAYMENT command message='{}'", message, ex);
            // In real systems:
            // 1️⃣ publish PaymentFailedEvent
            // 2️⃣ or send to DLQ
        }
    }
}
