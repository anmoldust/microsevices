package com.company.saga.order.messaging.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.company.saga.order.component.OrderSaga;
import com.company.saga.order.inbox.InboxEvent;
import com.company.saga.order.repository.InboxRepository;

import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class InventoryEventConsumer {

    private final OrderSaga saga;
    private final InboxRepository inboxRepository;

    public InventoryEventConsumer(OrderSaga saga, InboxRepository inboxRepository) {
        this.saga = saga;
        this.inboxRepository = inboxRepository;
    }

    @KafkaListener(topics = "inventory-events")
    @Transactional
    public void consume(String message, @Header(name = "eventId", required = false) byte[] eventIdBytes) {
        if (eventIdBytes == null) {
            return;
        }

        UUID eventId = UUID.fromString(new String(eventIdBytes, StandardCharsets.UTF_8));
        if (inboxRepository.existsById(eventId)) {
            return; // duplicate -> ignore
        }
        inboxRepository.save(new InboxEvent(eventId));

        String[] parts = message.split(":");
        UUID orderId = UUID.fromString(parts[0]);
        String status = parts[1];

        if ("SUCCESS".equals(status)) {
            saga.onInventorySuccess(orderId);
        } else {
            saga.onInventoryFailure(orderId);
        }
    }
}
