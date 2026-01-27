package com.company.saga.order.component;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.company.saga.order.domain.aggregate.Order;
import com.company.saga.order.outbox.OutboxEvent;
//import com.company.saga.order.outbox.OutboxRepository;
import com.company.saga.order.repository.OrderRepository;
import com.company.saga.order.repository.OrderSagaRepository;
import com.company.saga.order.repository.OutboxRepository;
import com.company.saga.order.saga.OrderSagaState;
//import com.company.saga.order.saga.SagaStep;

@Component
public class OrderSaga {

    private final OrderRepository orderRepository;
    private final OrderSagaRepository sagaRepository;
    private final OutboxRepository outboxRepository;

    public OrderSaga(OrderRepository orderRepository,
                     OrderSagaRepository sagaRepository,
                     OutboxRepository outboxRepository) {
        this.orderRepository = orderRepository;
        this.sagaRepository = sagaRepository;
        this.outboxRepository = outboxRepository;
    }

    /**
     * START SAGA
     */
    @Transactional
    public void start(UUID orderId) {
        sagaRepository.save(new OrderSagaState(orderId));
        OutboxEvent a=new OutboxEvent("payment-commands",
                orderId.toString());
        System.out.println("Created Outbox Event: "+a.isPublished());
        outboxRepository.save(
                a
                
        );
    }

    /**
     * PAYMENT SUCCESS
     */
    @Transactional
    public void onPaymentSuccess(UUID orderId) {
        OrderSagaState saga = sagaRepository.findById(orderId).orElseThrow();

        // ✅ Idempotency guard
        if (saga.isCompleted() || saga.isFailed()) {
            return;
        }

        saga.advanceTo(SagaStep.INVENTORY);
        sagaRepository.save(saga);

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.markPaymentCompleted();
        orderRepository.save(order);

        outboxRepository.save(
                new OutboxEvent(
                        "inventory-commands",
                        orderId.toString()
                )
        );
    }

    /**
     * PAYMENT FAILURE
     */
    @Transactional
    public void onPaymentFailure(UUID orderId) {
        OrderSagaState saga = sagaRepository.findById(orderId).orElseThrow();

        if (saga.isFailed()) {
            return;
        }

        saga.markFailed();
        sagaRepository.save(saga);

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.cancel();
        orderRepository.save(order);
    }

    /**
     * INVENTORY SUCCESS
     */
    @Transactional
    public void onInventorySuccess(UUID orderId) {
        OrderSagaState saga = sagaRepository.findById(orderId).orElseThrow();

        if (saga.isCompleted()) {
            return;
        }

        saga.markCompleted();
        sagaRepository.save(saga);

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.markInventoryReserved();
        order.complete();
        orderRepository.save(order);
    }

    /**
     * INVENTORY FAILURE → COMPENSATION
     */
    @Transactional
    public void onInventoryFailure(UUID orderId) {
        OrderSagaState saga = sagaRepository.findById(orderId).orElseThrow();

        if (saga.isFailed()) {
            return;
        }

        saga.markCompensating();
        sagaRepository.save(saga);

        // 🔁 Compensation command
        outboxRepository.save(
                new OutboxEvent(
                        "payment-refund-commands",
                        orderId.toString()
                )
        );

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.cancel();
        orderRepository.save(order);

        saga.markFailed();
        sagaRepository.save(saga);
    }
}
