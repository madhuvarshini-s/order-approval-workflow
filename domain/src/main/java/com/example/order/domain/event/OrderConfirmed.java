package com.example.order.domain.event;

import com.example.order.domain.model.Money;
import com.example.order.domain.model.OrderId;
import java.time.Instant;

/**
 * Domain Event: raised when an Order transitions DRAFT → CONFIRMED.
 */
public record OrderConfirmed(
        OrderId orderId,
        Instant occurredAt,
        Money orderTotal
) implements DomainEvent {}
