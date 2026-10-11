package com.example.order.adapters;

import com.example.order.adapters.inmemory.InMemoryNotificationPort;
import com.example.order.adapters.inmemory.InMemoryOrderRepository;
import com.example.order.adapters.inmemory.SettableClockPort;
import com.example.order.domain.event.OrderConfirmed;
import com.example.order.domain.model.Money;
import com.example.order.domain.model.Order;
import com.example.order.domain.model.OrderId;
import com.example.order.domain.model.OrderLine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryAdaptersTest {

    @Test
    @DisplayName("InMemoryOrderRepository stores and retrieves correctly")
    void repositoryStoreAndRetrieve() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        OrderId id = OrderId.of("ORD-ADAPT-1");
        Order order = Order.create(id, Instant.now());

        repo.save(order);

        assertThat(repo.findById(id)).isPresent();
        assertThat(repo.findById(id).get().getId()).isEqualTo(id);
        assertThat(repo.exists(id)).isTrue();
        assertThat(repo.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("InMemoryNotificationPort records published events")
    void notificationPortRecordsEvents() {
        InMemoryNotificationPort port = new InMemoryNotificationPort();
        OrderId id = OrderId.of("ORD-ADAPT-2");
        OrderConfirmed event = new OrderConfirmed(id, Instant.now(), Money.of("50.00", "USD"));

        port.publish(event);

        assertThat(port.count()).isEqualTo(1);
        assertThat(port.getPublishedEvents()).containsExactly(event);
    }

    @Test
    @DisplayName("SettableClockPort advances time as requested")
    void settableClockAdvances() {
        Instant t0 = Instant.parse("2026-10-11T00:00:00Z");
        SettableClockPort clock = SettableClockPort.fixed(t0);

        assertThat(clock.now()).isEqualTo(t0);
        clock.advance(Duration.ofMinutes(15));
        assertThat(clock.now()).isEqualTo(Instant.parse("2026-10-11T00:15:00Z"));
    }
}
