package com.company.saga.payment.domain.aggregate;

import jakarta.persistence.*;

import java.util.UUID;

import com.company.saga.payment.domain.valueobject.PaymentStatus;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private UUID paymentId;

    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    protected Payment() {}

    public Payment(UUID paymentId, UUID orderId) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.status = PaymentStatus.INITIATED;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void markSuccess() {
        this.status = PaymentStatus.SUCCESS;
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }
}