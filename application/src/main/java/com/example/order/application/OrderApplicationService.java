package com.example.order.application;

import com.example.order.domain.command.*;
import com.example.order.domain.event.DomainEvent;
import com.example.order.domain.model.Money;
import com.example.order.domain.model.Order;
import com.example.order.domain.model.OrderId;
import com.example.order.domain.model.OrderLine;
import com.example.order.domain.port.ClockPort;
import com.example.order.domain.port.NotificationPort;
import com.example.order.domain.port.OrderRepository;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * APPLICATION SERVICE — orchestrates use-cases for the Order aggregate.
 *
 * Responsibilities:
 *  1. Load/persist aggregate via the OrderRepository port.
 *  2. Delegate all business decisions to the Order aggregate.
 *  3. Publish domain events via the NotificationPort.
 *
 * No business logic lives here; it is purely coordination.
 * No Spring, JPA, or HTTP dependency.
 */
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final NotificationPort notificationPort;
    private final ClockPort clock;

    public OrderApplicationService(
            OrderRepository orderRepository,
            NotificationPort notificationPort,
            ClockPort clock) {
        this.orderRepository = orderRepository;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    // -------------------------------------------------------------------------
    // Use-case: Create a new Order
    // -------------------------------------------------------------------------

    public void createOrder(CreateOrderCommand cmd) {
        OrderId id = OrderId.of(cmd.orderId());
        Order order = Order.create(id, clock.now());
        orderRepository.save(order);
    }

    // -------------------------------------------------------------------------
    // Use-case: Add a line to a DRAFT Order
    // -------------------------------------------------------------------------

    public void addLine(AddLineCommand cmd) {
        Order order = loadOrThrow(cmd.orderId());
        OrderLine line = OrderLine.of(
                cmd.productId(),
                cmd.productName(),
                cmd.quantity(),
                Money.of(cmd.unitPrice(), cmd.currency())
        );
        order.addLine(line, clock.now());
        orderRepository.save(order);
    }

    // -------------------------------------------------------------------------
    // Use-case: Confirm an Order (DRAFT → CONFIRMED)
    // -------------------------------------------------------------------------

    public void confirmOrder(ConfirmOrderCommand cmd) {
        Order order = loadOrThrow(cmd.orderId());
        order.confirm(clock.now());
        orderRepository.save(order);
        publishAndClear(order);
    }

    // -------------------------------------------------------------------------
    // Use-case: Record payment (CONFIRMED → PAID)
    // -------------------------------------------------------------------------

    public void recordPayment(RecordPaymentCommand cmd) {
        Order order = loadOrThrow(cmd.orderId());
        order.recordPayment(clock.now());
        orderRepository.save(order);
        publishAndClear(order);
    }

    // -------------------------------------------------------------------------
    // Use-case: Cancel an Order
    // -------------------------------------------------------------------------

    public void cancelOrder(CancelOrderCommand cmd) {
        Order order = loadOrThrow(cmd.orderId());
        order.cancel(cmd.reason(), clock.now());
        orderRepository.save(order);
        publishAndClear(order);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Order loadOrThrow(String orderId) {
        return orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
    }

    private void publishAndClear(Order order) {
        List<DomainEvent> events = order.popDomainEvents();
        events.forEach(notificationPort::publish);
    }
}
