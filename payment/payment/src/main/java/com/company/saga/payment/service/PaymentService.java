package com.company.saga.payment.service;

//import com.example.payment.messaging.producer.PaymentEventProducer;
//import com.example.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import com.company.saga.payment.domain.aggregate.Payment;
import com.company.saga.payment.messaging.producer.PaymentEventProducer;
import com.company.saga.payment.repository.PaymentRepository;

import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentEventProducer producer;
    private final Random random = new Random();

    public PaymentService(PaymentRepository repository,
                          PaymentEventProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    public void processPayment(UUID orderId) {
        Payment payment = new Payment(UUID.randomUUID(), orderId);
        repository.save(payment);

        boolean success = true; // simulate failure

        if (success) {
            payment.markSuccess();
            repository.save(payment);
            producer.sendPaymentSuccess(orderId);
        } else {
            payment.markFailed();
            repository.save(payment);
            producer.sendPaymentFailure(orderId);
        }
    }

    public void refund(UUID orderId) {
        Payment payment = repository.findByOrderId(orderId)
                .orElseThrow();

        payment.refund();
        repository.save(payment);
    }
}