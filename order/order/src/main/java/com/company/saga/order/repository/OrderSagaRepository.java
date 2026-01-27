package com.company.saga.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.saga.order.saga.OrderSagaState;

import java.util.UUID;

public interface OrderSagaRepository extends JpaRepository<OrderSagaState, UUID> {
}