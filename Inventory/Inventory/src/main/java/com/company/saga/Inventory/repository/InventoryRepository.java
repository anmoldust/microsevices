package com.company.saga.Inventory.repository;
//package com.example.inventory.repository;

//import com.example.inventory.domain.aggregate.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import com.company.saga.Inventory.domain.aggregate.InventoryReservation;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryReservation, UUID> {
    Optional<InventoryReservation> findByOrderId(UUID orderId);
}
