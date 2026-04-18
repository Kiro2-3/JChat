package com.reloved.chat;

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
        return getMessages(100, 0);
    }

    public ObservableList<Message> getMessages(int limit, int offset) {
        ObservableList<Message> messages = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM messages ORDER BY timestamp DESC LIMIT ? OFFSET ?");
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                messages.add(0, new Message( // Add at top for correct chat order
                        rs.getString("id"),
                        rs.getString("remote_id"),
                        rs.getString("sender"),
                        rs.getString("content"),
                        rs.getString("timestamp"),
                        null,
                        rs.getInt("synced") == 1,
                        rs.getInt("retry_count"),
                        rs.getString("last_error")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public void sendMessage(String content) {
        String messageId = java.util.UUID.randomUUID().toString();
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Insert into messages table
                PreparedStatement psMsg = conn.prepareStatement(
                    "INSERT INTO messages (id, sender, content, synced) VALUES (?, ?, ?, 0)");
                psMsg.setString(1, messageId);
                psMsg.setString(2, "Me");
                psMsg.setString(3, content);
                psMsg.executeUpdate();

                // 2. Insert into sync_queue table
                PreparedStatement psQueue = conn.prepareStatement(
                    "INSERT INTO sync_queue (entity_type, entity_id, operation) VALUES (?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
                psQueue.setString(1, "MESSAGE");
                psQueue.setString(2, messageId);
                psQueue.setString(3, "SEND");
                psQueue.executeUpdate();

                ResultSet rs = psQueue.getGeneratedKeys();
                if (rs.next()) {
                    int queueId = rs.getInt(1);
                    // 3. Notify SyncService
                    SyncService.getInstance().addTask(new SyncService.SyncTask(
                        queueId, "MESSAGE", messageId, "SEND", null, 0, System.currentTimeMillis()));
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
