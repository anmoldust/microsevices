package com.company.saga.order.service;

import org.springframework.stereotype.Service;

import com.company.saga.order.component.OrderSaga;
import com.company.saga.order.domain.aggregate.Order;
import com.company.saga.order.repository.OrderRepository;

import jakarta.transaction.Transactional;

import java.util.UUID;

@Service
public class OrderCommandService {

    private final OrderRepository repository;
    private final OrderSaga saga;

    public OrderCommandService(OrderRepository repository,
                               OrderSaga saga) {
        this.repository = repository;
        this.saga = saga;
    }

    @Transactional
    public UUID createOrder() {
        UUID orderId = UUID.randomUUID();
        repository.save(new Order(orderId));
        saga.start(orderId);
        return orderId;
    }
}
