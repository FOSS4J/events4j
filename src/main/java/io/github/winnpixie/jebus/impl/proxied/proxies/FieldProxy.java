package io.github.winnpixie.jebus.impl.proxied.proxies;

import io.github.winnpixie.jebus.Handler;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class FieldProxy implements HandlerProxy {
    private Class<?> target;
    private Handler<?> handler;

    public FieldProxy(Object owner, Field field) {
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) return;

        ParameterizedType paramType = (ParameterizedType) genericType;
        Type actualType = paramType.getActualTypeArguments()[0];
        this.target = (Class<?>) actualType;

        try {
            this.handler = (Handler<?>) field.get(owner);
        } catch (IllegalArgumentException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public Class<?> getTarget() {
        return target;
    }

    @Override
    public Handler<?> getHandler() {
        return handler;
    }
}
