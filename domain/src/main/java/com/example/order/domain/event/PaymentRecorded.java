package com.example.order.domain.event;

import com.example.order.domain.model.Money;
import com.example.order.domain.model.OrderId;
import java.time.Instant;

/**
 * Domain Event: raised when payment is successfully recorded for an Order.
 * Transitions CONFIRMED → PAID.
 */
public record PaymentRecorded(
        OrderId orderId,
        Instant occurredAt,
        Money amountPaid
) implements DomainEvent {}
