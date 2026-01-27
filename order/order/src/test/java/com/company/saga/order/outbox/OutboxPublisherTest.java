package com.company.saga.order.outbox;

import com.company.saga.order.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxRepository repository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private OutboxEvent outboxEvent;

    private OutboxPublisher outboxPublisher;

    @BeforeEach
    void setUp() {
        outboxPublisher = new OutboxPublisher(repository, kafkaTemplate);
    }

    @Test
    void shouldPublishEvents() {
        // Given
        when(repository.findByPublishedFalseOrderByCreatedAtAsc()).thenReturn(List.of(outboxEvent));
        when(outboxEvent.getTopic()).thenReturn("test-topic");
        when(outboxEvent.getPayload()).thenReturn("test-payload");

        // Mock successful Kafka send
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.complete(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        // When
        outboxPublisher.publish();

        // Then
        verify(kafkaTemplate).send("test-topic", "test-payload");
        verify(outboxEvent).markPublished();
        verify(repository).save(outboxEvent);
    }

    @Test
    void shouldHandleEmptyOutbox() {
        when(repository.findByPublishedFalseOrderByCreatedAtAsc()).thenReturn(Collections.emptyList());

        outboxPublisher.publish();

        verifyNoInteractions(kafkaTemplate);
    }
}