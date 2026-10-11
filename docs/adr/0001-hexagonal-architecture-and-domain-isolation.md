# ADR 0001: Hexagonal Architecture and Zero Framework Domain Isolation

## Status
Accepted

## Context
The project requires modeling a non-trivial order approval workflow in Java while keeping domain rules strictly independent from frameworks, HTTP protocols, persistence mechanisms, and database adapters. We need a clear separation of concerns that ensures domain stability, testability, and technology agility.

## Decision
We adopt **Hexagonal Architecture (Ports and Adapters)** organized across a multi-module Maven build:

1. **`domain` module**:
   - Zero third-party dependencies (no Spring, JPA, Hibernate, or HTTP libraries).
   - Contains pure Java 17 constructs: Aggregate Root (`Order`), Entities, Value Objects (`OrderId`, `Money`, `OrderLine`), Domain Events (`OrderConfirmed`, `PaymentRecorded`, `OrderCancelled`), Domain Exceptions (`InvalidOrderStateException`), and outbound Ports (`OrderRepository`, `NotificationPort`, `ClockPort`).
   - Domain invariants and state machine rules are self-enforcing within the aggregate.

2. **`application` module**:
   - Orchestrates domain use cases (`OrderApplicationService`) via commands (`CreateOrderCommand`, `AddLineCommand`, etc.).
   - Interacts strictly through domain ports; does not know database implementations.

3. **`adapters` module**:
   - Provides concrete implementations for the domain ports (e.g., `InMemoryOrderRepository`, `InMemoryNotificationPort`, `SettableClockPort`).
   - Allows clean testing and modular replacement with production adapters (PostgreSQL, Kafka, REST) in the future without touching the core domain.

## Consequences
- **Positive**: Domain logic is 100% testable in milliseconds with JUnit 5 and AssertJ without booting Spring context or database containers.
- **Positive**: High resilience to framework or ORM breaking changes and migrations.
- **Trade-off**: Requires explicit mapping and port abstraction rather than convenient annotation-driven persistence (e.g. `@Entity`, `@Transactional`).
