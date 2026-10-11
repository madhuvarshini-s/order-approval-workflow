package com.example.order.domain.model;

import java.util.Objects;

/**
 * Value Object — a single line within an Order.
 * Immutable: once created, price and quantity cannot change.
 * Rule: Quantity must be a positive whole number.
 */
public final class OrderLine {

    private final String productId;
    private final String productName;
    private final int quantity;
    private final Money unitPrice;

    private OrderLine(String productId, String productName, int quantity, Money unitPrice) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID must not be blank");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive whole number, but was: " + quantity);
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price must not be null");
        }
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public static OrderLine of(String productId, String productName, int quantity, Money unitPrice) {
        return new OrderLine(productId, productName, quantity, unitPrice);
    }

    /**
     * Derived value: total for this line. Pure calculation from immutable fields.
     */
    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderLine other)) return false;
        return quantity == other.quantity
                && Objects.equals(productId, other.productId)
                && Objects.equals(productName, other.productName)
                && Objects.equals(unitPrice, other.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, quantity, unitPrice);
    }

    @Override
    public String toString() {
        return "OrderLine{product='" + productName + "', qty=" + quantity
                + ", unitPrice=" + unitPrice + ", lineTotal=" + lineTotal() + "}";
    }
}
