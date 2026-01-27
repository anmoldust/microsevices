package com.company.saga.order.inbox;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inbox")
public class InboxEvent {

    @Id
    private UUID eventId;

    private Instant processedAt;

    protected InboxEvent() {}

    public InboxEvent(UUID eventId) {
        this.eventId = eventId;
        this.processedAt = Instant.now();
    }
}