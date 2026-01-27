package com.company.saga.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.saga.order.inbox.InboxEvent;

import java.util.UUID;

public interface InboxRepository extends JpaRepository<InboxEvent, UUID> {
}