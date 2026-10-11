package com.example.order.domain.event;

import com.example.order.domain.model.OrderId;
import java.time.Instant;

/**
 * Domain Event: raised when an Order is cancelled.
 * Legal transitions: DRAFT → CANCELLED, CONFIRMED → CANCELLED.
 */
public record OrderCancelled(
        OrderId orderId,
        Instant occurredAt,
        String reason
) implements DomainEvent {}
