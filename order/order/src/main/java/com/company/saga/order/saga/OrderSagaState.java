package com.company.saga.order.saga;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

import com.company.saga.order.component.SagaStep;

@Entity
@Table(name = "order_saga")
public class OrderSagaState {

    @Id
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private SagaStep currentStep;

    @Enumerated(EnumType.STRING)
    private SagaStatus status;

    private int retryCount;

    private Instant lastUpdatedAt;

    protected OrderSagaState() {}

    public OrderSagaState(UUID orderId) {
        this.orderId = orderId;
        this.currentStep = SagaStep.PAYMENT;
        this.status = SagaStatus.STARTED;
        this.retryCount = 0;
        this.lastUpdatedAt = Instant.now();
    }

    // ===== getters =====

    public UUID getOrderId() {
        return orderId;
    }

    public SagaStep getCurrentStep() {
        return currentStep;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    // ===== state helpers (IDEMPOTENCY) =====

    public boolean isCompleted() {
        return status == SagaStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == SagaStatus.FAILED;
    }

    // ===== transitions =====

    public void advanceTo(SagaStep nextStep) {
        this.currentStep = nextStep;
        this.retryCount = 0;
        this.status = SagaStatus.IN_PROGRESS;
        this.lastUpdatedAt = Instant.now();
    }

    public void incrementRetry() {
        this.retryCount++;
        this.lastUpdatedAt = Instant.now();
    }

    public void markCompleted() {
        this.status = SagaStatus.COMPLETED;
        this.lastUpdatedAt = Instant.now();
    }

    public void markCompensating() {
        this.status = SagaStatus.COMPENSATING;
        this.lastUpdatedAt = Instant.now();
    }

    public void markFailed() {
        this.status = SagaStatus.FAILED;
        this.lastUpdatedAt = Instant.now();
    }
}
