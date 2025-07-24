package io.github.winnpixie.jebus.impl.proxied.proxies;

import io.github.winnpixie.jebus.Handler;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MethodProxy implements HandlerProxy {
    private static final Map<Class<?>, MethodHandles.Lookup> LOOKUP_CACHE = new HashMap<>();

    private final Class<?> target;
    private final Handler<?> handler;

    private MethodHandle handle;
    private BiConsumer<Object, Object> callback;

    public MethodProxy(Object owner, Method method) {
        this.target = method.getParameterTypes()[0];
        this.handler = value -> {
            try {
                if (callback != null) {
                    callback.accept(owner, value);
                } else if (handle != null) {
                    handle.invoke(owner, value);
                }
            } catch (Throwable thrown) {
                thrown.printStackTrace();
            }
        };

        try {
            MethodHandles.Lookup lookup = getLookup(owner.getClass());
            this.handle = lookup.unreflect(method);

            CallSite site = LambdaMetafactory.metafactory(lookup,
                    "accept",
                    MethodType.methodType(BiConsumer.class),
                    MethodType.methodType(void.class, Object.class, Object.class),
                    handle,
                    handle.type());

            this.callback = (BiConsumer<Object, Object>) site.getTarget().invokeExact();
        } catch (Throwable thrown) {
            thrown.printStackTrace();
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

    private static MethodHandles.Lookup getLookup(Class<?> cls) {
        return LOOKUP_CACHE.computeIfAbsent(cls, caller -> {
            try {
                Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                constructor.setAccessible(true);
                return constructor.newInstance(caller, -1);
            } catch (Exception ignored) {
                return MethodHandles.lookup();
            }
        });
    }
}
