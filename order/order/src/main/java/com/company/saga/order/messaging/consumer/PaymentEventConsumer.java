package com.company.saga.order.messaging.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import 	com.company.saga.order.repository.*;
import com.company.saga.order.component.OrderSaga;
import com.company.saga.order.inbox.InboxEvent;

import jakarta.transaction.Transactional;

import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private final OrderSaga saga;

    public PaymentEventConsumer(OrderSaga saga) {
        this.saga = saga;
    }

    @KafkaListener(topics = "payment-events")
    public void consume(String message) {
        String[] parts = message.split(":");
        UUID orderId = UUID.fromString(parts[0]);
        String status = parts[1];

        if ("SUCCESS".equals(status)) {
            saga.onPaymentSuccess(orderId);
        } else {
            saga.onPaymentFailure(orderId);
        }
    }
}

//@Component
//public class PaymentEventConsumer {
//
//    private final OrderSaga saga;
//    private final InboxRepository inboxRepository;
//
//    public PaymentEventConsumer(OrderSaga saga,InboxRepository inboxRepository) {
//        this.saga = saga;
//        this.inboxRepository = inboxRepository;
//    }
//
//    @KafkaListener(topics = "payment-events")
//    @Transactional
//    public void consume(String message, @Header("eventId") String eventId) {
//
//        UUID uuid = UUID.fromString(eventId);
//
//        if (inboxRepository.existsById(uuid)) {
//            return; // duplicate → ignore
//        }
//
//        inboxRepository.save(new InboxEvent(uuid));
//
//        String[] parts = message.split(":");
//        UUID orderId = UUID.fromString(parts[0]);
//        String status = parts[1];
//
//        if ("SUCCESS".equals(status)) {
//            saga.onPaymentSuccess(orderId);
//        } else {
//            saga.onPaymentFailure(orderId);
//        }
//    }
//
//}
