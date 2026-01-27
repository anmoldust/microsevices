package com.company.saga.Inventory.messaging.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InventoryEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public InventoryEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendInventoryReserved(UUID orderId) {
        kafkaTemplate.send("inventory-events", orderId + ":SUCCESS");
    }

    public void sendInventoryFailed(UUID orderId) {
        kafkaTemplate.send("inventory-events", orderId + ":FAILED");
    }
}