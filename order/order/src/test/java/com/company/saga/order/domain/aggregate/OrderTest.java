package com.company.saga.order.domain.aggregate;

import com.company.saga.order.domain.valueobject.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void shouldCreateNewOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId);

        assertEquals(orderId, order.getOrderId());
        assertEquals(OrderStatus.NEW, order.getStatus());
    }

    @Test
    void shouldMarkPaymentCompleted() {
        Order order = new Order(UUID.randomUUID());
        order.markPaymentCompleted();
        assertEquals(OrderStatus.PAYMENT_COMPLETED, order.getStatus());
    }

    @Test
    void shouldMarkInventoryReserved() {
        Order order = new Order(UUID.randomUUID());
        order.markInventoryReserved();
        assertEquals(OrderStatus.INVENTORY_RESERVED, order.getStatus());
    }

    @Test
    void shouldCompleteOrder() {
        Order order = new Order(UUID.randomUUID());
        order.complete();
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void shouldCancelOrder() {
        Order order = new Order(UUID.randomUUID());
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }
}