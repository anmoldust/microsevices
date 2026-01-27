package com.company.saga.Inventory.domain.event;

import java.util.UUID;

public record InventoryFailedEvent(UUID orderId) {}