package com.company.saga.Inventory.messaging.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class InventoryEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public InventoryEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendInventoryReserved(UUID orderId) {
        sendWithEventId("inventory-events", orderId + ":SUCCESS");
    }

    public void sendInventoryFailed(UUID orderId) {
        sendWithEventId("inventory-events", orderId + ":FAILED");
    }

    private void sendWithEventId(String topic, String payload) {
        String eventId = UUID.randomUUID().toString();
        RecordHeaders headers = new RecordHeaders();
        headers.add("eventId", eventId.getBytes(StandardCharsets.UTF_8));

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, payload, headers);
        kafkaTemplate.send(record);
    }
}
