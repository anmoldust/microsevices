package com.company.saga.order.messaging.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.company.saga.order.component.OrderSaga;
import com.company.saga.order.inbox.InboxEvent;
import com.company.saga.order.repository.InboxRepository;

import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private final OrderSaga saga;
    private final InboxRepository inboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    public PaymentEventConsumer(
            OrderSaga saga,
            InboxRepository inboxRepository,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.saga = saga;
        this.inboxRepository = inboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "payment-events")
    @Transactional
    public void consume(
            String message,
            @Header(name = "eventId", required = false) byte[] eventIdBytes,
            @Header(name = KafkaHeaders.RECEIVED_TOPIC, required = false) String topic
    ) {
        if (eventIdBytes == null) {
            sendToDlq(topic, "missing_eventId", message);
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
            saga.onPaymentSuccess(orderId);
        } else {
            saga.onPaymentFailure(orderId);
        }
    }

    private void sendToDlq(String topic, String reason, String payload) {
        String safeTopic = topic == null ? "unknown-topic" : topic;
        String dlqPayload = "{\"topic\":\"" + safeTopic
                + "\",\"reason\":\"" + reason
                + "\",\"payload\":\"" + escapeJson(payload) + "\"}";
        kafkaTemplate.send("order-dead-letter", dlqPayload);
        log.warn("Sent to DLQ: {}", dlqPayload);
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
