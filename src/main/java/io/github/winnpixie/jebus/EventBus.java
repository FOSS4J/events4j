package io.github.winnpixie.jebus;

public interface EventBus {
    <T> void subscribe(Class<T> type, Handler<T> handler);

    <T> void unsubscribe(Handler<T> handler);

    <T> T post(T value);
}
