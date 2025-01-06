package com.milabuda.redditconnector.api.client;

public class InitialFullScanState {
    private static final InitialFullScanState INSTANCE = new InitialFullScanState();
    private boolean initialFullScanDone = false;

    public InitialFullScanState() {}

    public static InitialFullScanState getInstance() {
        return INSTANCE;
    }

    public boolean isInitialFullScanDone() {
        return initialFullScanDone;
    }

    public void markInitialFullScanDone() {
        this.initialFullScanDone = true;
    }
}