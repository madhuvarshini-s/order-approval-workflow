package com.example.order.domain.port;

import com.example.order.domain.model.Order;
import com.example.order.domain.model.OrderId;

import java.util.Optional;

/**
 * PORT — Driven port for Order persistence.
 *
 * The domain defines this interface; infrastructure implements it.
 * This keeps the domain free of JPA / SQL dependencies.
 */
public interface OrderRepository {

    /**
     * Persist or update an order.
     */
    void save(Order order);

    /**
     * Find an order by its identity. Returns empty when not found.
     */
    Optional<Order> findById(OrderId id);

    /**
     * Check existence without loading the full aggregate.
     */
    default boolean exists(OrderId id) {
        return findById(id).isPresent();
    }
}
