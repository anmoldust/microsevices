package com.company.saga.order.domain.aggregate;

import jakarta.persistence.*;

import java.util.UUID;

import com.company.saga.order.domain.valueobject.OrderStatus;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    protected Order() {}

    public Order(UUID orderId) {
        this.orderId = orderId;
        this.status = OrderStatus.NEW;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void markPaymentCompleted() {
        this.status = OrderStatus.PAYMENT_COMPLETED;
    }

    public void markInventoryReserved() {
        this.status = OrderStatus.INVENTORY_RESERVED;
    }

    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }
}