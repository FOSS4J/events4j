package io.github.winnpixie.jebus.impl.proxied.proxies;

import io.github.winnpixie.jebus.Handler;

public interface HandlerProxy<T> {
    Class<T> getTarget();

    Handler<T> getHandler();
}
