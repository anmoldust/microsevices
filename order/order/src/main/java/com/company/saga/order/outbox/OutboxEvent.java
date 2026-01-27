package com.company.saga.order.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox")
public class OutboxEvent {

    @Id
    private UUID eventId;

    private String topic;

    @Lob
    private String payload;

    private boolean published;

    private Instant createdAt;

    protected OutboxEvent() {}

    public OutboxEvent(String topic, String payload) {
        this.eventId = UUID.randomUUID();
        this.topic = topic;
        this.payload = payload;
        this.published = false;
        this.createdAt = Instant.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getTopic() {
        return topic;
    }

    public String getPayload() {
        return payload;
    }

    public boolean isPublished() {
        return published;
    }

    public void markPublished() {
        this.published = true;
    }
}