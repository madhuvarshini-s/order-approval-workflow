# Order Approval Workflow Domain

A clean, non-trivial Domain Model and Approval Workflow implementation in Java 17, demonstrating Hexagonal Architecture (Ports and Adapters), Domain-Driven Design (DDD), and Architecture Decision Records (ADRs).

## Key Characteristics & Architecture Constraints
- **Pure Domain Module**: Zero framework dependencies (no Spring, JPA, HTTP, or DB annotations/drivers).
- **Enforced Invariants**:
  - An order must contain at least one line before confirmation.
  - Quantity must be a positive whole number.
  - A cancelled order cannot be paid.
  - A paid order cannot return to draft.
  - Order total is derived from immutable line prices and quantities.
- **Ports & Adapters**: Dedicated ports for Persistence (`OrderRepository`), Notifications (`NotificationPort`), and Time (`ClockPort`), accompanied by in-memory adapters for test execution.
- **Documented Decisions**: Architecture Decision Records located under `docs/adr/`.

---

## Project Structure

```
approval-workflow/
├── pom.xml                               # Maven Multi-Module Root POM
├── docs/
│   └── adr/
│       ├── 0001-hexagonal-architecture-and-domain-isolation.md
│       ├── 0002-order-approval-workflow-state-machine.md
│       └── 0003-ports-for-persistence-notifications-and-time.md
├── domain/                               # Pure Domain Module (Zero frameworks)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/order/domain/
│       │   ├── model/                    # Order, OrderId, OrderLine, Money, OrderStatus
│       │   ├── command/                  # CreateOrderCommand, AddLineCommand, ConfirmOrderCommand, etc.
│       │   ├── event/                    # DomainEvent, OrderConfirmed, PaymentRecorded, OrderCancelled
│       │   ├── port/                     # OrderRepository, NotificationPort, ClockPort
│       │   └── exception/                # InvalidOrderStateException
│       └── test/java/com/example/order/domain/
│           └── OrderDomainTest.java      # Legal and illegal state transitions JUnit 5 suite
├── application/                          # Application Services (Use-case orchestration)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/order/application/
│       │   └── OrderApplicationService.java
│       └── test/java/com/example/order/application/
│           └── OrderApplicationServiceTest.java
└── adapters/                             # In-Memory Adapters for Ports
    ├── pom.xml
    └── src/
        ├── main/java/com/example/order/adapters/inmemory/
        │   ├── InMemoryOrderRepository.java
        │   ├── InMemoryNotificationPort.java
        │   └── SettableClockPort.java
        └── test/java/com/example/order/adapters/
            └── InMemoryAdaptersTest.java
```

---

## State Transition Rules

```
       +---------------------------------------------+
       |                                             |
       v                                             v
  [ DRAFT ] ------------> [ CONFIRMED ] -------> [ PAID ]
       |                         |
       |                         |
       +-------> [ CANCELLED ] <-+
```

### Legal Transitions
- `DRAFT` $\to$ `CONFIRMED` (Requires at least 1 line item)
- `CONFIRMED` $\to$ `PAID` (Emits `PaymentRecorded`)
- `DRAFT` $\to$ `CANCELLED` (Emits `OrderCancelled`)
- `CONFIRMED` $\to$ `CANCELLED` (Emits `OrderCancelled`)

### Illegal Transitions
- `CANCELLED` $\to$ `PAID` (Throws `InvalidOrderStateException`)
- `PAID` $\to$ `DRAFT` (Throws `InvalidOrderStateException`)
- `PAID` $\to$ `CANCELLED` (Throws `InvalidOrderStateException`)
- `CANCELLED` $\to$ `CONFIRMED` (Throws `InvalidOrderStateException`)
- `DRAFT` $\to$ `PAID` (Throws `InvalidOrderStateException`)

---

## Build and Test

Requirements: Java 17+ and Maven 3.8+

```bash
mvn clean test
```
