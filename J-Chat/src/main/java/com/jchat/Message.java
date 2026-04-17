package com.jchat;

import java.util.UUID;

public class Message {
    private String id;
    private String remoteId;
    private String sender;
    private String content;
    private String timestamp;
    private String avatarUrl;
    private boolean synced;

    public Message(String sender, String content, String timestamp, String avatarUrl) {
        this(UUID.randomUUID().toString(), null, sender, content, timestamp, avatarUrl, false);
    }

    public Message(String id, String remoteId, String sender, String content, String timestamp, String avatarUrl, boolean synced) {
        this.id = id;
        this.remoteId = remoteId;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.avatarUrl = avatarUrl;
        this.synced = synced;
    }

    public String getId() { return id; }
    public String getRemoteId() { return remoteId; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public String getAvatarUrl() { return avatarUrl; }
    public boolean isSynced() { return synced; }

    public void setRemoteId(String remoteId) { this.remoteId = remoteId; }
    public void setSynced(boolean synced) { this.synced = synced; }
}
