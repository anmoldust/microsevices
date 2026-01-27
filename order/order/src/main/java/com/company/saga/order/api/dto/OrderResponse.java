package com.company.saga.order.api.dto;

import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String status
) {}