package com.example.order.domain;

import com.example.order.domain.event.DomainEvent;
import com.example.order.domain.event.OrderCancelled;
import com.example.order.domain.event.OrderConfirmed;
import com.example.order.domain.event.PaymentRecorded;
import com.example.order.domain.exception.InvalidOrderStateException;
import com.example.order.domain.model.Money;
import com.example.order.domain.model.Order;
import com.example.order.domain.model.OrderId;
import com.example.order.domain.model.OrderLine;
import com.example.order.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderDomainTest {

    private final Instant baseTime = Instant.parse("2026-10-11T10:00:00Z");
    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.create(OrderId.of("ORD-1001"), baseTime);
    }

    @Nested
    @DisplayName("Creation & Draft Invariants")
    class CreationAndDraftRules {

        @Test
        @DisplayName("Order starts in DRAFT status with no lines and zero total")
        void orderStartsInDraft() {
            assertThat(order.getStatus()).isEqualTo(OrderStatus.DRAFT);
            assertThat(order.getLines()).isEmpty();
            assertThat(order.calculateTotal()).isEqualTo(Money.zero("USD"));
            assertThat(order.getCreatedAt()).isEqualTo(baseTime);
            assertThat(order.getUpdatedAt()).isEqualTo(baseTime);
        }

        @Test
        @DisplayName("Rule: Quantity must be a positive whole number")
        void orderLineRejectsZeroOrNegativeQuantity() {
            assertThatThrownBy(() -> OrderLine.of("P1", "Item", 0, Money.of("10.00", "USD")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive whole number");

            assertThatThrownBy(() -> OrderLine.of("P1", "Item", -5, Money.of("10.00", "USD")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive whole number");
        }

        @Test
        @DisplayName("Lines can be added in DRAFT state and order total is derived correctly")
        void addLinesCalculatesTotalDerived() {
            order.addLine(OrderLine.of("P1", "Mouse", 2, Money.of("25.00", "USD")), baseTime.plusSeconds(10));
            order.addLine(OrderLine.of("P2", "Keyboard", 1, Money.of("50.00", "USD")), baseTime.plusSeconds(20));

            assertThat(order.getLines()).hasSize(2);
            // 2 * 25.00 + 1 * 50.00 = 100.00 USD
            assertThat(order.calculateTotal()).isEqualTo(Money.of("100.00", "USD"));
            assertThat(order.getUpdatedAt()).isEqualTo(baseTime.plusSeconds(20));
        }

        @Test
        @DisplayName("Rule: An order must contain at least one line before confirmation")
        void confirmWithoutLinesThrowsException() {
            assertThatThrownBy(() -> order.confirm(baseTime.plusSeconds(30)))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("has no lines");
        }
    }

    @Nested
    @DisplayName("Legal State Transitions")
    class LegalTransitions {

        @Test
        @DisplayName("Transition: DRAFT -> CONFIRMED raises OrderConfirmed event")
        void confirmOrderWithLines() {
            order.addLine(OrderLine.of("P1", "Monitor", 1, Money.of("200.00", "USD")), baseTime.plusSeconds(10));
            order.confirm(baseTime.plusSeconds(30));

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            List<DomainEvent> events = order.popDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(OrderConfirmed.class);
            OrderConfirmed event = (OrderConfirmed) events.get(0);
            assertThat(event.orderId()).isEqualTo(order.getId());
            assertThat(event.orderTotal()).isEqualTo(Money.of("200.00", "USD"));
            assertThat(event.occurredAt()).isEqualTo(baseTime.plusSeconds(30));
        }

        @Test
        @DisplayName("Transition: CONFIRMED -> PAID raises PaymentRecorded event")
        void payConfirmedOrder() {
            order.addLine(OrderLine.of("P1", "Laptop", 1, Money.of("1000.00", "USD")), baseTime.plusSeconds(10));
            order.confirm(baseTime.plusSeconds(20));
            order.popDomainEvents(); // clear previous

            order.recordPayment(baseTime.plusSeconds(40));

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            List<DomainEvent> events = order.popDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(PaymentRecorded.class);
            PaymentRecorded event = (PaymentRecorded) events.get(0);
            assertThat(event.amountPaid()).isEqualTo(Money.of("1000.00", "USD"));
        }

        @Test
        @DisplayName("Transition: DRAFT -> CANCELLED raises OrderCancelled event")
        void cancelDraftOrder() {
            order.cancel("Customer changed mind", baseTime.plusSeconds(15));

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            List<DomainEvent> events = order.popDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(OrderCancelled.class);
            OrderCancelled event = (OrderCancelled) events.get(0);
            assertThat(event.reason()).isEqualTo("Customer changed mind");
        }

        @Test
        @DisplayName("Transition: CONFIRMED -> CANCELLED raises OrderCancelled event")
        void cancelConfirmedOrder() {
            order.addLine(OrderLine.of("P1", "Laptop", 1, Money.of("1000.00", "USD")), baseTime.plusSeconds(10));
            order.confirm(baseTime.plusSeconds(20));
            order.popDomainEvents();

            order.cancel("Stock unavailable", baseTime.plusSeconds(30));

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            List<DomainEvent> events = order.popDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(OrderCancelled.class);
        }
    }

    @Nested
    @DisplayName("Illegal State Transitions & Boundary Invariants")
    class IllegalTransitions {

        @Test
        @DisplayName("Rule 3: A cancelled order cannot be paid")
        void cancelledOrderCannotBePaid() {
            order.cancel("Cancelled early", baseTime.plusSeconds(10));

            assertThatThrownBy(() -> order.recordPayment(baseTime.plusSeconds(20)))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("CANCELLED");
        }

        @Test
        @DisplayName("Rule 4: A paid order cannot return to draft or add lines")
        void paidOrderCannotModifyLines() {
            order.addLine(OrderLine.of("P1", "Item", 1, Money.of("10.00", "USD")), baseTime.plusSeconds(5));
            order.confirm(baseTime.plusSeconds(10));
            order.recordPayment(baseTime.plusSeconds(15));

            assertThatThrownBy(() -> order.addLine(OrderLine.of("P2", "Extra", 1, Money.of("5.00", "USD")), baseTime.plusSeconds(20)))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("expected status DRAFT");
        }

        @Test
        @DisplayName("Paid order cannot be cancelled")
        void paidOrderCannotBeCancelled() {
            order.addLine(OrderLine.of("P1", "Item", 1, Money.of("10.00", "USD")), baseTime.plusSeconds(5));
            order.confirm(baseTime.plusSeconds(10));
            order.recordPayment(baseTime.plusSeconds(15));

            assertThatThrownBy(() -> order.cancel("Refund requested", baseTime.plusSeconds(20)))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("PAID");
        }

        @Test
        @DisplayName("Cancelled order cannot be confirmed")
        void cancelledOrderCannotBeConfirmed() {
            order.addLine(OrderLine.of("P1", "Item", 1, Money.of("10.00", "USD")), baseTime.plusSeconds(5));
            order.cancel("Abort", baseTime.plusSeconds(10));

            assertThatThrownBy(() -> order.confirm(baseTime.plusSeconds(20)))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("expected status DRAFT but was CANCELLED");
        }

        @Test
        @DisplayName("Draft order cannot be paid directly without confirmation")
        void draftOrderCannotBePaidDirectly() {
            order.addLine(OrderLine.of("P1", "Item", 1, Money.of("10.00", "USD")), baseTime.plusSeconds(5));

            assertThatThrownBy(() -> order.recordPayment(baseTime.plusSeconds(10)))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("must be CONFIRMED first");
        }

        @Test
        @DisplayName("Cannot add lines once order is confirmed")
        void cannotAddLinesAfterConfirmed() {
            order.addLine(OrderLine.of("P1", "Item", 1, Money.of("10.00", "USD")), baseTime.plusSeconds(5));
            order.confirm(baseTime.plusSeconds(10));

            assertThatThrownBy(() -> order.addLine(OrderLine.of("P2", "Another", 1, Money.of("20.00", "USD")), baseTime.plusSeconds(15)))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("expected status DRAFT but was CONFIRMED");
        }
    }
}
