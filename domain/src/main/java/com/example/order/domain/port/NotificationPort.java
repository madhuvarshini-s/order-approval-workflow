package com.example.order.domain.port;

import com.example.order.domain.event.DomainEvent;

/**
 * PORT — Driven port for sending notifications/events outside the domain.
 *
 * Implementations may dispatch to message brokers, email services, etc.
 * The domain only knows the port; it is agnostic of the delivery mechanism.
 */
public interface NotificationPort {

    /**
     * Publish a domain event to any interested parties.
     *
     * @param event the event that occurred
     */
    void publish(DomainEvent event);
}
