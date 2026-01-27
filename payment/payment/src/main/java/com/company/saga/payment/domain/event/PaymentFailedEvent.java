package com.company.saga.payment.domain.event;

import java.util.UUID;

public record PaymentFailedEvent(UUID orderId) {}
