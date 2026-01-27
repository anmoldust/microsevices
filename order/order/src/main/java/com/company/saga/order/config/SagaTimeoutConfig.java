package com.company.saga.order.config;

public class SagaTimeoutConfig {

    public static final int MAX_RETRIES = 3;
    public static final long STEP_TIMEOUT_SECONDS = 10;

    private SagaTimeoutConfig() {}
}