package io.github.foss4j.events4j.impl;

import io.github.foss4j.events4j.Subscriber;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class FieldSubscriber implements ReflectSubscriber {
    private Class<?> target;
    private Subscriber<?> subscriber;

    public FieldSubscriber(Object parent, Field field) {
        Type genericType = field.getGenericType();
        System.out.println(genericType.getTypeName());
        if (!(genericType instanceof ParameterizedType)) return;

        ParameterizedType paramType = (ParameterizedType) genericType;
        Type actualType = paramType.getActualTypeArguments()[0];
        this.target = (Class<?>) actualType;

        try {
            this.subscriber = (Subscriber<?>) field.get(parent);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class<?> getTarget() {
        return target;
    }

    @Override
    public Subscriber<?> getSubscriber() {
        return subscriber;
    }
}
