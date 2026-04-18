package com.reloved;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class OfflineSyncTest {

    @BeforeEach
    public void setup() throws Exception {
        // Clear database before each test
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM messages");
            stmt.executeUpdate("DELETE FROM sync_queue");
        }
    }

    @Test
    public void testOfflineMessagePersistenceAndRetry() throws Exception {
        // 1. Mock 'Failed Network' state
        NetworkService.getInstance().setOnline(false);

        // 2. Send a message while offline
        String testContent = "Test Offline Message";
        MessageRepository.getInstance().sendMessage(testContent);

        // 3. Verify it is saved locally and NOT synced
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check messages table
            ResultSet rsMsg = stmt.executeQuery("SELECT * FROM messages WHERE content = '" + testContent + "'");
            assertTrue(rsMsg.next(), "Message should be saved in the local database");
            assertEquals(0, rsMsg.getInt("synced"), "Message should not be marked as synced");
            String messageId = rsMsg.getString("id");

            // Check sync_queue table
            ResultSet rsQueue = stmt.executeQuery("SELECT * FROM sync_queue WHERE entity_id = '" + messageId + "'");
            assertTrue(rsQueue.next(), "Message should be in the sync queue");
        }

        // 4. Mock 'Network Restored' state
        NetworkService.getInstance().setOnline(true);

        // 5. Trigger Sync Manual Retry (Simulating the background task)
        SyncService.getInstance().syncPendingChanges();

        // 6. Give it a moment to process (since MockRemoteDataSource has 1s latency)
        Thread.sleep(2000);

        // 7. Verify it is now synced
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rsSynced = stmt.executeQuery("SELECT * FROM messages WHERE content = '" + testContent + "'");
            assertTrue(rsSynced.next());
            assertEquals(1, rsSynced.getInt("synced"), "Message should be marked as synced after reconnect");
            assertNotNull(rsSynced.getString("remote_id"), "Remote ID should be populated");

            // Verify queue is cleared
            ResultSet rsQueueEmpty = stmt.executeQuery("SELECT COUNT(*) FROM sync_queue");
            rsQueueEmpty.next();
            assertEquals(0, rsQueueEmpty.getInt(1), "Sync queue should be empty after successful sync");
        }
    }
}
