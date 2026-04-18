package com.jchat.service;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class NetworkService {
    private static final NetworkService instance = new NetworkService();
    private final BooleanProperty online = new SimpleBooleanProperty(true);

    private NetworkService() {}

    public static NetworkService getInstance() {
        return instance;
    }

    public BooleanProperty onlineProperty() {
        return online;
    }

    public boolean isOnline() {
        return online.get();
    }

    public void setOnline(boolean online) {
        this.online.set(online);
    }
}
