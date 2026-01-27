package com.company.saga.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.saga.order.outbox.OutboxEvent;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByPublishedFalse();
    List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();
    
}