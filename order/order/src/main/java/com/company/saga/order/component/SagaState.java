package com.company.saga.order.component;

public enum SagaState {
    STARTED,
    PAYMENT_COMPLETED,
    INVENTORY_RESERVED,
    COMPLETED,
    COMPENSATING,
    CANCELLED
}
