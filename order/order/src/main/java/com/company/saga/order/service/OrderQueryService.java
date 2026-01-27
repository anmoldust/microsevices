package com.company.saga.order.service;

import org.springframework.stereotype.Service;

import com.company.saga.order.domain.aggregate.Order;
import com.company.saga.order.repository.OrderRepository;

import java.util.Optional;
import java.util.UUID;

//@Service
//public class OrderQueryService {
//
//    private final OrderRepository repository;
//
//    public OrderQueryService(OrderRepository repository) {
//        this.repository = repository;
//    }
//
//    public Optional<Order> getOrder(UUID orderId) {
//        return repository.findById(orderId);
//    }
//}
@Service
public class OrderQueryService {

    private final OrderRepository repository;

    public OrderQueryService(OrderRepository repository) {
        this.repository = repository;
    }

    public Order getById(UUID orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + orderId
                ));
    }
}