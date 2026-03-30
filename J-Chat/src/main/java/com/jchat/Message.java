package com.jchat;

import java.time.LocalDateTime;

public class Message {
    private String sender;
    private String content;
    private String timestamp;
    private String avatarUrl;

    public Message(String sender, String content, String timestamp, String avatarUrl) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.avatarUrl = avatarUrl;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
