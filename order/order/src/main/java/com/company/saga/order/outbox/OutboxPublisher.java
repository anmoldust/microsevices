package com.company.saga.order.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.company.saga.order.repository.OutboxRepository;

@Component
public class OutboxPublisher {
    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);
    private static final int BATCH_SIZE = 50;
    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxRepository repository,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void publish() {
        for (OutboxEvent event : repository.findByPublishedFalseOrderByCreatedAtAsc(PageRequest.of(0, BATCH_SIZE))) {
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
