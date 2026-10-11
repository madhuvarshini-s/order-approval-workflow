package com.example.order.domain.command;

import java.math.BigDecimal;

/**
 * Command: intent to add a line item to an existing draft Order.
 */
public record AddLineCommand(
        String orderId,
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        String currency
) {
    public AddLineCommand {
        if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("orderId required");
        if (productId == null || productId.isBlank()) throw new IllegalArgumentException("productId required");
        if (productName == null || productName.isBlank()) throw new IllegalArgumentException("productName required");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("unitPrice must not be negative");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency required");
    }
}
