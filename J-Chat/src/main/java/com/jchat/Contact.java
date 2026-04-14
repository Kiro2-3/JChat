package com.jchat;

public class Contact {
    private final String name;
    private final String lastMessage;
    private final String avatarUrl;
    private final String status;

    public Contact(String name, String lastMessage, String status) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.status = status;
        this.avatarUrl = "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&background=random&color=fff";
    }

    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getStatus() { return status; }
}
