package com.reloved;

public class ConflictException extends Exception {
    private final Object serverVersion;

    public ConflictException(String message, Object serverVersion) {
        super(message);
        this.serverVersion = serverVersion;
    }

    public Object getServerVersion() {
        return serverVersion;
    }
}
