package io.github.foss4j.events4j.impl;

import io.github.foss4j.events4j.Subscriber;

public interface ReflectSubscriber<T> {
    Class<T> getTarget();

    Subscriber<T> getSubscriber();
}
