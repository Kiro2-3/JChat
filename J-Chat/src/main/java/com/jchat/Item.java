package com.jchat;

public class Item {
    private int id;
    private String title;
    private String description;
    private String type; // "SELL" or "WANTED"

    public Item(int id, String title, String description, String type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getType() { return type; }
}