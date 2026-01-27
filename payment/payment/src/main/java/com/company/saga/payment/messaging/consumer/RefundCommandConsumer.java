package com.company.saga.payment.messaging.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.company.saga.payment.service.PaymentService;

import java.util.UUID;

@Component
public class RefundCommandConsumer {

    private final PaymentService paymentService;

    public RefundCommandConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "payment-refund-commands")
    public void consume(String message) {
        UUID orderId = UUID.fromString(message);
        paymentService.refund(orderId);
    }
}