package com.jchat;

import javafx.application.Platform;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SyncService {
    private static final SyncService instance = new SyncService();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private SyncService() {
        startSyncTask();
    }

    public static SyncService getInstance() {
        return instance;
    }

    private void startSyncTask() {
        scheduler.scheduleAtFixedRate(() -> {
            if (NetworkService.getInstance().isOnline()) {
                syncMessages();
                syncContacts();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private static final int MAX_RETRIES = 3;

    private void syncMessages() {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM messages WHERE synced = 0 AND retry_count < ?");
            ps.setInt(1, MAX_RETRIES);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String sender = rs.getString("sender");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");
                int retryCount = rs.getInt("retry_count");

                Message msg = new Message(id, null, sender, content, timestamp, null, false);
                try {
                    String remoteId = MockRemoteDataSource.getInstance().pushMessage(msg);
                    updateMessageSyncStatus(id, remoteId);
                } catch (Exception e) {
                    System.err.println("Failed to sync message " + id + ": " + e.getMessage());
                    handleSyncFailure(id, e.getMessage(), retryCount + 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSyncFailure(String localId, String errorMessage, int newRetryCount) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE messages SET retry_count = ?, last_error = ? WHERE id = ?");
            ps.setInt(1, newRetryCount);
            ps.setString(2, errorMessage);
            ps.setString(3, localId);
            ps.executeUpdate();
            
            if (newRetryCount >= MAX_RETRIES) {
                System.err.println("Message " + localId + " has reached max retries and will not be synced automatically.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateMessageSyncStatus(String localId, String remoteId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE messages SET remote_id = ?, synced = 1 WHERE id = ?");
            ps.setString(1, remoteId);
            ps.setString(2, localId);
            ps.executeUpdate();
            System.out.println("Synced message " + localId + " with remote ID " + remoteId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void syncContacts() {
        // Simple sync: just fetch and replace for now
        try {
            var remoteContacts = MockRemoteDataSource.getInstance().fetchContacts();
            try (Connection conn = DatabaseManager.getConnection()) {
                // This is a simplified sync logic
                for (Contact c : remoteContacts) {
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT OR REPLACE INTO contacts (id, name, lastMessage, status, avatarUrl, synced) VALUES (?, ?, ?, ?, ?, 1)");
                    ps.setString(1, c.getId());
                    ps.setString(2, c.getName());
                    ps.setString(3, c.getLastMessage());
                    ps.setString(4, c.getStatus());
                    ps.setString(5, c.getAvatarUrl());
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to sync contacts: " + e.getMessage());
        }
    }
}
