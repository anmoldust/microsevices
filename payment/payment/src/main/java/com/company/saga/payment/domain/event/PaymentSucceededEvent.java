package com.company.saga.payment.domain.event;

import java.util.UUID;

public record PaymentSucceededEvent(UUID orderId) {}