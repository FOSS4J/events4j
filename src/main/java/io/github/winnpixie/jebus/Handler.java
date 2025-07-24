package io.github.winnpixie.jebus;

public interface Handler<T> {
    void handle(T value);
}
