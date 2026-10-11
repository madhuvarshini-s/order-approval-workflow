package com.example.order.adapters.inmemory;

import com.example.order.domain.model.Order;
import com.example.order.domain.model.OrderId;
import com.example.order.domain.port.OrderRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-Memory adapter for OrderRepository.
 *
 * Stores orders in a plain HashMap — no database, no JPA.
 * Used in unit and integration tests; can also serve as a lightweight runtime stub.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> store = new HashMap<>();

    @Override
    public void save(Order order) {
        store.put(order.getId().getValue(), order);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(store.get(id.getValue()));
    }

    /** Convenience method for test assertions. */
    public int size() {
        return store.size();
    }

    /** Clear all stored orders. */
    public void clear() {
        store.clear();
    }
}
