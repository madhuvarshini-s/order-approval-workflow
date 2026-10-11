# ADR 0003: Deterministic Ports for Clocks, Notifications, and Persistence

## Status
Accepted

## Context
Non-deterministic side effects (such as direct calls to `System.currentTimeMillis()` / `Instant.now()`, direct HTTP calls, or direct SQL calls) hinder testing, make timing-dependent business rules flaky, and couple the core business model to external infrastructure.

## Decision
1. **ClockPort**: Abstract system time into `com.example.order.domain.port.ClockPort`.
   - Production systems can use wall-clock time (`Instant.now()`).
   - Unit and domain tests use `SettableClockPort`, enabling reproducible timestamps and time progression tests.
2. **OrderRepository Port**: Outbound port returning pure domain models wrapped in `Optional<Order>`.
   - Tested using `InMemoryOrderRepository` backed by standard Java collections.
3. **NotificationPort**: Outbound port dispatching sealed domain events (`DomainEvent`).
   - Tested using `InMemoryNotificationPort` allowing straightforward assertion of event counts and payload verification without message brokers.

## Consequences
- **Positive**: Tests run deterministically without external infrastructure dependencies.
- **Positive**: Complete decoupling between domain events and messaging brokers (e.g. Kafka, RabbitMQ, SNS).
