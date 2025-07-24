package io.github.winnpixie.jebus.impl.proxied;

import io.github.winnpixie.jebus.EventBus;
import io.github.winnpixie.jebus.Handler;
import io.github.winnpixie.jebus.impl.proxied.proxies.FieldProxy;
import io.github.winnpixie.jebus.impl.proxied.proxies.HandlerProxy;
import io.github.winnpixie.jebus.impl.proxied.proxies.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProxiedEventBus implements EventBus {
    private static final Map<Object, List<HandlerProxy<?>>> PROXY_CACHE = new ConcurrentHashMap<>();

    private final Map<Class<?>, List<Handler<?>>> subscriptions = new ConcurrentHashMap<>();

    public void subscribeAll(Object owner) {
        List<HandlerProxy<?>> cache = PROXY_CACHE.get(owner);
        if (cache != null) {
            for (HandlerProxy<?> subscriber : cache) {
                subscribeProxy(subscriber);
            }

            return;
        }

        cache = new ArrayList<>();

        proxyMethods(owner, cache);
        proxyFields(owner, cache);

        PROXY_CACHE.put(owner, cache);
    }

    private void proxyMethods(Object owner, List<HandlerProxy<?>> cache) {
        for (Method method : owner.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)
                    || method.getParameterCount() != 1) continue;

            if (!method.isAccessible()) method.setAccessible(true);

            MethodProxy proxy = new MethodProxy(owner, method);
            subscribeProxy(proxy);
            cache.add(proxy);
        }
    }

    private void proxyFields(Object owner, List<HandlerProxy<?>> cache) {
        for (Field field : owner.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Subscribe.class)) continue;

            if (!field.isAccessible()) field.setAccessible(true);

            FieldProxy proxy = new FieldProxy(owner, field);
            subscribeProxy(proxy);
            cache.add(proxy);
        }
    }

    private <T> void subscribeProxy(HandlerProxy<T> wrapper) {
        subscribe(wrapper.getTarget(), wrapper.getHandler());
    }

    public <T> void subscribe(Class<T> type, Handler<T> handler) {
        subscriptions.computeIfAbsent(type, v -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    public void unsubscribeAll(Object parent) {
        List<HandlerProxy<?>> proxies = PROXY_CACHE.get(parent);
        if (proxies == null) return;

        for (HandlerProxy<?> wrapper : proxies) {
            unsubscribe(wrapper.getHandler());
        }
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
