package com.jchat;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageRepository {
    private static final MessageRepository instance = new MessageRepository();

    private MessageRepository() {}

    public static MessageRepository getInstance() {
        return instance;
    }

    public ObservableList<Message> getMessages() {
        ObservableList<Message> messages = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM messages ORDER BY timestamp ASC");
            while (rs.next()) {
                messages.add(new Message(
                        rs.getString("id"),
                        rs.getString("remote_id"),
                        rs.getString("sender"),
                        rs.getString("content"),
                        rs.getString("timestamp"),
                        null,
                        rs.getInt("synced") == 1
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public void sendMessage(String content) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO messages (sender, content, synced) VALUES (?, ?, 0)");
            ps.setString(1, "Me");
            ps.setString(2, content);
            ps.executeUpdate();
            
            // Trigger sync immediately if online
            if (NetworkService.getInstance().isOnline()) {
                new Thread(() -> {
                    // In a real app, you'd trigger SyncService to process only this message
                    // For now, we rely on the periodic SyncService task
                }).start();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
