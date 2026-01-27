package com.company.saga.order.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.company.saga.order.messaging.producer.OrderCommandProducer;
import com.company.saga.order.repository.OutboxRepository;

import jakarta.transaction.Transactional;

@Component
public class OutboxPublisher {
    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxRepository repository,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 50000, initialDelay = 50000)
    public void publish() {
        for (OutboxEvent event : repository.findByPublishedFalseOrderByCreatedAtAsc()) {
            try {
                var result = kafkaTemplate.send(event.getTopic(), event.getPayload()).get();
                
                log.info(
                    "Message published by Messaging Queue | topic={} | partition={} | offset={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
                );

                event.markPublished();
                repository.save(event);
            } catch (Exception ex) {
                log.error("Failed to send command for eventId={}", event.getPayload(), ex);
                // Stop processing to preserve order and retry later
                break;
            }
        }
    }
}
