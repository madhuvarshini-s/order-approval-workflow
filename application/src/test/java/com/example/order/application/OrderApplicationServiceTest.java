package com.example.order.application;

import com.example.order.adapters.inmemory.InMemoryNotificationPort;
import com.example.order.adapters.inmemory.InMemoryOrderRepository;
import com.example.order.adapters.inmemory.SettableClockPort;
import com.example.order.domain.command.*;
import com.example.order.domain.event.OrderCancelled;
import com.example.order.domain.event.OrderConfirmed;
import com.example.order.domain.event.PaymentRecorded;
import com.example.order.domain.model.Money;
import com.example.order.domain.model.Order;
import com.example.order.domain.model.OrderId;
import com.example.order.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderApplicationServiceTest {

    private InMemoryOrderRepository orderRepository;
    private InMemoryNotificationPort notificationPort;
    private SettableClockPort clock;
    private OrderApplicationService service;

    private final Instant startTime = Instant.parse("2026-10-11T12:00:00Z");

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepository();
        notificationPort = new InMemoryNotificationPort();
        clock = SettableClockPort.fixed(startTime);
        service = new OrderApplicationService(orderRepository, notificationPort, clock);
    }

    @Test
    @DisplayName("End-to-end happy path: create -> add line -> confirm -> pay")
    void completeOrderLifecycle() {
        String orderId = "ORD-E2E-1";

        // 1. Create Order
        service.createOrder(new CreateOrderCommand(orderId));
        Order order = orderRepository.findById(OrderId.of(orderId)).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(order.getCreatedAt()).isEqualTo(startTime);

        // 2. Add Line
        service.addLine(new AddLineCommand(
                orderId,
                "PROD-1",
                "Wireless Headset",
                2,
                new BigDecimal("75.00"),
                "USD"
        ));
        order = orderRepository.findById(OrderId.of(orderId)).orElseThrow();
        assertThat(order.getLines()).hasSize(1);
        assertThat(order.calculateTotal()).isEqualTo(Money.of("150.00", "USD"));

        // 3. Confirm Order
        service.confirmOrder(new ConfirmOrderCommand(orderId));
        order = orderRepository.findById(OrderId.of(orderId)).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(notificationPort.getPublishedEvents()).hasSize(1);
        assertThat(notificationPort.getPublishedEvents().get(0)).isInstanceOf(OrderConfirmed.class);

        // 4. Record Payment
        service.recordPayment(new RecordPaymentCommand(orderId));
        order = orderRepository.findById(OrderId.of(orderId)).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(notificationPort.getPublishedEvents()).hasSize(2);
        assertThat(notificationPort.getPublishedEvents().get(1)).isInstanceOf(PaymentRecorded.class);
    }

    @Test
    @DisplayName("Cancel order flow publishes OrderCancelled event")
    void cancelOrderFlow() {
        String orderId = "ORD-CANCEL-1";
        service.createOrder(new CreateOrderCommand(orderId));
        service.addLine(new AddLineCommand(
                orderId, "P1", "Item 1", 1, new BigDecimal("20.00"), "USD"
        ));
        service.cancelOrder(new CancelOrderCommand(orderId, "Requested by customer"));

        Order order = orderRepository.findById(OrderId.of(orderId)).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(notificationPort.getPublishedEvents()).hasSize(1);
        assertThat(notificationPort.getPublishedEvents().get(0)).isInstanceOf(OrderCancelled.class);
    }

    @Test
    @DisplayName("Throws exception if order is not found")
    void orderNotFound() {
        assertThatThrownBy(() -> service.confirmOrder(new ConfirmOrderCommand("UNKNOWN")))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Order not found");
    }
}
