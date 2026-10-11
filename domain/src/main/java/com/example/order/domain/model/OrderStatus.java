package com.example.order.domain.model;

/**
 * Lifecycle states of an Order aggregate.
 *
 * Legal transitions:
 *   DRAFT      → CONFIRMED  (confirm)
 *   DRAFT      → CANCELLED  (cancel)
 *   CONFIRMED  → PAID       (recordPayment)
 *   CONFIRMED  → CANCELLED  (cancel)
 *
 * Illegal transitions (enforced by aggregate):
 *   CANCELLED  → PAID       (A cancelled order cannot be paid)
 *   PAID       → DRAFT      (A paid order cannot return to draft)
 *   PAID       → CANCELLED  (immutable once paid)
 *   CANCELLED  → CONFIRMED  (immutable once cancelled)
 */
public enum OrderStatus {
    DRAFT,
    CONFIRMED,
    PAID,
    CANCELLED
}
