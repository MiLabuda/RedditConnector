package com.milabuda.redditconnector.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SimpleDIContainer {
    private final Map<Class<?>, Supplier<?>> services = new HashMap<>();

    public <T> void register(Class<T> serviceClass, Supplier<T> supplier) {
        services.put(serviceClass, supplier);
    }

    public <T> T resolve(Class<T> serviceClass) {
        Supplier<?> supplier = services.get(serviceClass);
        if (supplier == null) {
            throw new IllegalArgumentException("No service registered for: " + serviceClass.getName());
        }
        return serviceClass.cast(supplier.get());
    }
}