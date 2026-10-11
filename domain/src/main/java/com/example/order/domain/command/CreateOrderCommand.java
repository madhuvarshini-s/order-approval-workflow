package com.example.order.domain.command;

/**
 * Command: intent to create a new Order.
 * Commands are pure data transfer objects with no behaviour.
 */
public record CreateOrderCommand(String orderId) {
    public CreateOrderCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
    }
}
