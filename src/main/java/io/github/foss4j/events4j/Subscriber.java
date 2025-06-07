package io.github.foss4j.events4j;

public interface Subscriber<T> {
    void handle(T value);
}
