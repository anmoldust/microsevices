package com.company.saga.order.domain.valueobject;

public enum OrderStatus {
    NEW,
    PAYMENT_COMPLETED,
    INVENTORY_RESERVED,
    COMPLETED,
    CANCELLED
}