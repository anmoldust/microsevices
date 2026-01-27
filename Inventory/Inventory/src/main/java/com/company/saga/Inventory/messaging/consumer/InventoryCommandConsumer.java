package com.company.saga.Inventory.messaging.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.company.saga.Inventory.service.InventoryService;

import java.util.UUID;

@Component
public class InventoryCommandConsumer {

    private final InventoryService inventoryService;

    public InventoryCommandConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "inventory-commands")
    public void consume(String message) {
        UUID orderId = UUID.fromString(message);
        inventoryService.reserve(orderId);
    }
}