package com.company.saga.Inventory.service;
import org.springframework.stereotype.Service;

import com.company.saga.Inventory.domain.aggregate.InventoryReservation;
import com.company.saga.Inventory.messaging.producer.InventoryEventProducer;
import com.company.saga.Inventory.repository.InventoryRepository;

import java.util.Random;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository repository;
    private final InventoryEventProducer producer;
    private final Random random = new Random();

    public InventoryService(InventoryRepository repository,
                            InventoryEventProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    public void reserve(UUID orderId) {
        boolean success = random.nextBoolean(); // simulate failure

        InventoryReservation reservation =
                new InventoryReservation(UUID.randomUUID(), orderId);

        if (success) {
            repository.save(reservation);
            producer.sendInventoryReserved(orderId);
        } else {
            reservation.markFailed();
            repository.save(reservation);
            producer.sendInventoryFailed(orderId);
        }
    }

    public void release(UUID orderId) {
        InventoryReservation reservation = repository.findByOrderId(orderId)
                .orElseThrow();

        reservation.release();
        repository.save(reservation);
    }
}