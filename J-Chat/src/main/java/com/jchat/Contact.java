package com.jchat;

import java.util.UUID;

public class Contact {
    private final String id;
    private final String name;
    private final String lastMessage;
    private final String avatarUrl;
    private final String status;
    private final boolean synced;

    public Contact(String name, String lastMessage, String status) {
        this(UUID.randomUUID().toString(), name, lastMessage, status, true);
    }

    public Contact(String id, String name, String lastMessage, String status, boolean synced) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.status = status;
        this.synced = synced;
        this.avatarUrl = "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&background=random&color=fff";
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getStatus() { return status; }
    public boolean isSynced() { return synced; }
}
