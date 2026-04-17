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
    private int retryCount;
    private String lastError;
    private String mediaUrl;
    private boolean mediaDownloaded;

    public Message(String sender, String content, String timestamp, String avatarUrl) {
        this(UUID.randomUUID().toString(), null, sender, content, timestamp, avatarUrl, false, 0, null, null, false);
    }

    public Message(String id, String remoteId, String sender, String content, String timestamp, String avatarUrl, boolean synced, int retryCount, String lastError) {
        this(id, remoteId, sender, content, timestamp, avatarUrl, synced, retryCount, lastError, null, false);
    }

    public Message(String id, String remoteId, String sender, String content, String timestamp, String avatarUrl, boolean synced, int retryCount, String lastError, String mediaUrl, boolean mediaDownloaded) {
        this.id = id;
        this.remoteId = remoteId;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.avatarUrl = avatarUrl;
        this.synced = synced;
        this.retryCount = retryCount;
        this.lastError = lastError;
        this.mediaUrl = mediaUrl;
        this.mediaDownloaded = mediaDownloaded;
    }

    public String getId() { return id; }
    public String getRemoteId() { return remoteId; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public String getAvatarUrl() { return avatarUrl; }
    public boolean isSynced() { return synced; }
    public int getRetryCount() { return retryCount; }
    public String getLastError() { return lastError; }
    public String getMediaUrl() { return mediaUrl; }
    public boolean isMediaDownloaded() { return mediaDownloaded; }

    public void setRemoteId(String remoteId) { this.remoteId = remoteId; }
    public void setSynced(boolean synced) { this.synced = synced; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public void setMediaDownloaded(boolean mediaDownloaded) { this.mediaDownloaded = mediaDownloaded; }

    public SyncStatus getSyncStatus() {
        if (synced) {
            return SyncStatus.SENT; // In a full app, you'd check a 'delivered' flag for DELIVERED
        } else if (retryCount >= 3) {
            return SyncStatus.FAILED;
        } else {
            return SyncStatus.PENDING;
        }
    }
}
