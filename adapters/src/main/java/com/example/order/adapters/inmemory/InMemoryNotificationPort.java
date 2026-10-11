package com.example.order.adapters.inmemory;

import com.example.order.domain.event.DomainEvent;
import com.example.order.domain.port.NotificationPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-Memory adapter for NotificationPort.
 *
 * Collects published events in memory for test assertions.
 */
public class InMemoryNotificationPort implements NotificationPort {

    private final List<DomainEvent> publishedEvents = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        publishedEvents.add(event);
    }

    public List<DomainEvent> getPublishedEvents() {
        return Collections.unmodifiableList(publishedEvents);
    }

    public int count() {
        return publishedEvents.size();
    }

    public void clear() {
        publishedEvents.clear();
    }
}
