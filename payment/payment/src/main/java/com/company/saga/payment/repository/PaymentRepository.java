package com.company.saga.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.saga.payment.domain.aggregate.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);
}