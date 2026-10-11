package com.example.order.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object — monetary amount with currency.
 * Immutable. Arithmetic operations return new instances.
 */
public final class Money {

    private final BigDecimal amount;
    private final String currency;

    private Money(BigDecimal amount, String currency) {
        if (amount == null) throw new IllegalArgumentException("Amount must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Amount must not be negative");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency must not be blank");
        this.amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
        this.currency = currency.toUpperCase();
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money multiply(int factor) {
        if (factor < 0) throw new IllegalArgumentException("Factor must not be negative");
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot operate on different currencies: " + this.currency + " vs " + other.currency);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return Objects.equals(amount, other.amount) && Objects.equals(currency, other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
