package com.company.saga.Inventory.domain.aggregate;

import jakarta.persistence.*;

import java.util.UUID;

import com.company.saga.Inventory.domain.valueobject.InventoryStatus;

@Entity
@Table(name = "inventory_reservations")
public class InventoryReservation {

    @Id
    private UUID reservationId;

    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    protected InventoryReservation() {}

    public InventoryReservation(UUID reservationId, UUID orderId) {
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.status = InventoryStatus.RESERVED;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public void markFailed() {
        this.status = InventoryStatus.FAILED;
    }

    public void release() {
        this.status = InventoryStatus.RELEASED;
    }
}