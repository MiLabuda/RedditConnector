package com.milabuda.redditconnector.api.client;

import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CustomLogger extends Logger {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CustomLogger.class);

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        log.info("Request: {} {}", request.httpMethod(), request.url());
        log.info("Headers: {}", request.headers());
        if (request.body() != null) {
            log.info("Body: {}", new String(request.body()));
        }
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Logger.Level logLevel, Response response, long elapsedTime) throws IOException {
        String reason = response.reason() != null && logLevel.compareTo(Logger.Level.NONE) > 0 ? " " + response.reason() : "";
        int status = response.status();
        log.info("<--- HTTP/1.1 {}{} ({}ms)", status, reason, elapsedTime);

        // Logging headers
        for (String field : response.headers().keySet()) {
            for (String value : Util.valuesOrEmpty(response.headers(), field)) {
                log.info("{}: {}", field, value);
            }
        }
        log.info("<--- END HTTP ({}-byte body)", response.body().length());

        return response;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        log.info(methodTag(configKey) + format, args);
    }
}