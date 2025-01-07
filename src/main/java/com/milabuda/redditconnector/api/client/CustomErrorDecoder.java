package com.milabuda.redditconnector.api.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CustomErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        String errorMessage = String.format("Feign Client Error - Method: %s, Status: %d, Reason: %s",
                methodKey, response.status(), Optional.ofNullable(response.reason()).orElse("Unknown"));

        logger.error(errorMessage);
        return new RuntimeException(errorMessage);
    }
}