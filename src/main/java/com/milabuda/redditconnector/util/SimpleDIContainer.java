package com.milabuda.redditconnector.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SimpleDIContainer {
    private static final Logger log = LoggerFactory.getLogger(SimpleDIContainer.class);

    private final Map<Class<?>, Supplier<?>> services = new HashMap<>();

    public <T> void register(Class<T> serviceClass, Supplier<T> supplier) {
        log.debug("Registering service: {}", serviceClass.getName());
        services.put(serviceClass, wrapWithLogging(serviceClass, supplier));
        log.info("Successfully registered service: {}", serviceClass.getSimpleName());
    }

    public <T> T resolve(Class<T> serviceClass) {
        log.debug("Resolving service: {}", serviceClass.getName());
        Supplier<?> supplier = services.get(serviceClass);
        if (supplier == null) {
            log.error("Failed to resolve service: {}", serviceClass.getName());
            throw new IllegalArgumentException("No service registered for: " + serviceClass.getName());
        }
        T resolvedObject = serviceClass.cast(supplier.get());
        if (resolvedObject == null) {
            log.error("Resolved service is null for: {}", serviceClass.getName());
        } else {
            log.info("Successfully resolved: {}", serviceClass.getSimpleName());
        }
        return resolvedObject;
    }

    private <T> Supplier<T> wrapWithLogging(Class<T> serviceClass, Supplier<T> supplier) {
        return () -> {
            log.debug("Initializing service: {}", serviceClass.getName());
            try {
                T instance = supplier.get();
                if (instance == null) {
                    log.error("Supplier returned null for: {}", serviceClass.getSimpleName());
                } else {
                    log.info("Initialized service: {}", serviceClass.getSimpleName());
                }
                return instance;
            } catch (Exception e) {
                log.error("Exception while initializing service: {}, Error: {}", serviceClass.getSimpleName(), e.getMessage(), e);
                throw e;
            }
        };
    }
}