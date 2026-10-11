# ADR 0002: State Machine and Domain Invariant Enforcement in the Aggregate Root

## Status
Accepted

## Context
The order domain requirements specify exact state transitions and invariant rules:
- State set: `DRAFT`, `CONFIRMED`, `PAID`, `CANCELLED`
- An order must contain at least one line before confirmation.
- Quantity must be a positive whole number.
- A cancelled order cannot be paid.
- A paid order cannot return to draft.
- Order total is derived from immutable line prices and quantities.

We needed to decide how and where to enforce these rules.

## Decision
1. **Encapsulation in Aggregate Root**: The `Order` aggregate root strictly guards all state transitions and invariant validation. No external service can mutate the status or lines directly.
2. **Immutability of Value Objects**:
   - `OrderLine`, `Money`, and `OrderId` are fully immutable.
   - `OrderLine` enforces positive quantities at instantiation time.
   - The total price is derived on-demand via pure functional methods (`calculateTotal`), ensuring zero risk of stale derived state.
3. **Explicit Domain Exceptions**: Any illegal transition (such as attempting to pay a cancelled order, or confirming an empty order) throws `InvalidOrderStateException`.
4. **Domain Events for Side Effects**: When valid state transitions occur (`confirm()`, `recordPayment()`, `cancel()`), domain events (`OrderConfirmed`, `PaymentRecorded`, `OrderCancelled`) are staged within the aggregate and dispatched upon transaction completion.

## Consequences
- **Positive**: Invariants cannot be bypassed regardless of how the domain is called.
- **Positive**: Clear auditability through explicit domain events.
- **Trade-off**: Callers must handle domain exceptions appropriately.
