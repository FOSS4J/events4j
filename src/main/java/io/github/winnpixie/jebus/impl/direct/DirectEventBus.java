package io.github.winnpixie.jebus.impl.direct;

import io.github.winnpixie.jebus.EventBus;
import io.github.winnpixie.jebus.Handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DirectEventBus implements EventBus {
    private final Map<Class<?>, List<Handler<?>>> subscriptions = new ConcurrentHashMap<>();

    public <T> void subscribe(Class<T> type, Handler<T> handler) {
        subscriptions.computeIfAbsent(type, v -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    @Override
    public <T> void unsubscribe(Handler<T> handler) {
        for (List<Handler<?>> handlers : subscriptions.values()) {
            handlers.remove(handler);
        }
    }

    @Override
    public <T> T post(T value) {
        List<Handler<?>> subscribers = subscriptions.get(value.getClass());
        if (subscribers == null) return value;

        for (Handler<?> handler : subscribers) {
            ((Handler<T>) handler).handle(value);
        }

        return value;
    }
}
