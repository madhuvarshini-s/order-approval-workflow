package com.example.order.domain.model;

import com.example.order.domain.event.DomainEvent;
import com.example.order.domain.event.OrderCancelled;
import com.example.order.domain.event.OrderConfirmed;
import com.example.order.domain.event.PaymentRecorded;
import com.example.order.domain.exception.InvalidOrderStateException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ORDER AGGREGATE ROOT
 *
 * Encapsulates all business rules for the Order approval workflow:
 *
 *  Rule 1 – An order must contain at least one line before confirmation.
 *  Rule 2 – Quantity must be a positive whole number (enforced in OrderLine).
 *  Rule 3 – A cancelled order cannot be paid.
 *  Rule 4 – A paid order cannot return to draft.
 *  Rule 5 – Order total is derived from immutable line prices and quantities.
 *
 * Domain events are collected internally and consumed by the application layer.
 * This class has NO Spring, JPA, or HTTP imports.
 */
public class Order {

    private final OrderId id;
    private final List<OrderLine> lines;
    private OrderStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    // Domain events raised during this transaction
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    /**
     * Create a new Order in DRAFT status. Private constructor forces use of factory.
     */
    private Order(OrderId id, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = createdAt;
        this.lines = new ArrayList<>();
        this.status = OrderStatus.DRAFT;
    }

    public static Order create(OrderId id, Instant createdAt) {
        return new Order(id, createdAt);
    }

    // -------------------------------------------------------------------------
    // Behaviour — mutating commands
    // -------------------------------------------------------------------------

    /**
     * Add a line to the order. Only allowed while in DRAFT.
     */
    public void addLine(OrderLine line, Instant now) {
        requireStatus(OrderStatus.DRAFT, "add a line to");
        Objects.requireNonNull(line, "line must not be null");
        lines.add(line);
        touch(now);
    }

    /**
     * Confirm the order.
     * Rule: Order must have at least one line.
     * Legal from: DRAFT → CONFIRMED
     */
    public void confirm(Instant now) {
        requireStatus(OrderStatus.DRAFT, "confirm");
        if (lines.isEmpty()) {
            throw new InvalidOrderStateException(
                    "Cannot confirm order " + id + ": it has no lines. Add at least one line first.");
        }
        status = OrderStatus.CONFIRMED;
        touch(now);
        raiseDomainEvent(new OrderConfirmed(id, now, calculateTotal()));
    }

    /**
     * Record payment.
     * Rule: A cancelled order cannot be paid.
     * Legal from: CONFIRMED → PAID
     */
    public void recordPayment(Instant now) {
        if (status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    "Cannot record payment for order " + id + ": it is already CANCELLED.");
        }
        if (status == OrderStatus.PAID) {
            throw new InvalidOrderStateException(
                    "Cannot record payment for order " + id + ": it is already PAID.");
        }
        if (status == OrderStatus.DRAFT) {
            throw new InvalidOrderStateException(
                    "Cannot record payment for order " + id + ": it must be CONFIRMED first (currently DRAFT).");
        }
        status = OrderStatus.PAID;
        touch(now);
        raiseDomainEvent(new PaymentRecorded(id, now, calculateTotal()));
    }

    /**
     * Cancel the order.
     * Rule: A paid order cannot be cancelled (it cannot return to draft either).
     * Legal from: DRAFT → CANCELLED, CONFIRMED → CANCELLED
     */
    public void cancel(String reason, Instant now) {
        if (status == OrderStatus.PAID) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order " + id + ": it is already PAID.");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order " + id + ": it is already CANCELLED.");
        }
        status = OrderStatus.CANCELLED;
        touch(now);
        raiseDomainEvent(new OrderCancelled(id, now, reason));
    }

    // -------------------------------------------------------------------------
    // Queries — pure derivations, no side effects
    // -------------------------------------------------------------------------

    /**
     * Rule 5: Order total is derived from immutable line prices and quantities.
     * If there are no lines the total is zero in the first line's currency, defaulting to "USD".
     */
    public Money calculateTotal() {
        if (lines.isEmpty()) {
            return Money.zero("USD");
        }
        String currency = lines.get(0).getUnitPrice().getCurrency();
        return lines.stream()
                .map(OrderLine::lineTotal)
                .reduce(Money.zero(currency), Money::add);
    }

    // -------------------------------------------------------------------------
    // Domain events
    // -------------------------------------------------------------------------

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /** Consume events (clear after publishing). */
    public List<DomainEvent> popDomainEvents() {
        List<DomainEvent> copy = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(copy);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public OrderId getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public List<OrderLine> getLines() { return Collections.unmodifiableList(lines); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void requireStatus(OrderStatus required, String action) {
        if (status != required) {
            throw new InvalidOrderStateException(
                    "Cannot " + action + " order " + id + ": expected status " + required
                            + " but was " + status + ".");
        }
    }

    private void raiseDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    private void touch(Instant now) {
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", status=" + status + ", lines=" + lines.size()
                + ", total=" + calculateTotal() + "}";
    }
}
