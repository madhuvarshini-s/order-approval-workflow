package com.example.order.domain.event;

import com.example.order.domain.model.OrderId;
import java.time.Instant;

/**
 * Marker interface for all domain events emitted by the Order aggregate.
 * Domain events are immutable records of something that happened.
 */
public sealed interface DomainEvent
        permits OrderConfirmed, PaymentRecorded, OrderCancelled {

    OrderId orderId();
    Instant occurredAt();
}
