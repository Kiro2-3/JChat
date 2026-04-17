package com.jchat;

import javafx.application.Platform;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.*;

public class SyncService {
    private static final SyncService instance = new SyncService();
    private final PriorityBlockingQueue<SyncTask> taskQueue = new PriorityBlockingQueue<>();
    private final ExecutorService workerExecutor = Executors.newSingleThreadExecutor();
    private static final int MAX_RETRIES = 3;

    private SyncService() {
        loadPendingTasksFromDb();
        startWorkerThread();
        // Keep the periodic refresh for contacts
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::syncContacts, 0, 1, TimeUnit.MINUTES);
    }

    public static SyncService getInstance() {
        return instance;
    }

    private void startWorkerThread() {
        workerExecutor.submit(() -> {
            while (true) {
                try {
                    SyncTask task = taskQueue.take(); // Blocks until a task is available
                    if (NetworkService.getInstance().isOnline()) {
                        processTask(task);
                    } else {
                        // If offline, put it back or wait for network
                        taskQueue.put(task);
                        Thread.sleep(5000); // Wait before retrying
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addTask(SyncTask task) {
        taskQueue.offer(task);
    }

    public void retryTask(String entityId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM sync_queue WHERE entity_id = ?");
            ps.setString(1, entityId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SyncTask task = new SyncTask(
                        rs.getInt("id"),
                        rs.getString("entity_type"),
                        rs.getString("entity_id"),
                        rs.getString("operation"),
                        rs.getString("payload"),
                        0, // Reset retry count
                        System.currentTimeMillis()
                );
                // Update DB as well
                PreparedStatement psUpd = conn.prepareStatement("UPDATE sync_queue SET retry_count = 0, last_error = NULL WHERE id = ?");
                psUpd.setInt(1, task.getQueueId());
                psUpd.executeUpdate();
                
                addTask(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPendingTasksFromDb() {
        try (Connection conn = DatabaseManager.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM sync_queue ORDER BY timestamp ASC");
            while (rs.next()) {
                taskQueue.offer(new SyncTask(
                        rs.getInt("id"),
                        rs.getString("entity_type"),
                        rs.getString("entity_id"),
                        rs.getString("operation"),
                        rs.getString("payload"),
                        rs.getInt("retry_count"),
                        rs.getTimestamp("timestamp").getTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processTask(SyncTask task) {
        if (task.getRetryCount() >= MAX_RETRIES) return;

        try {
            if ("MESSAGE".equals(task.getEntityType()) && "SEND".equals(task.getOperation())) {
                Message msg = getMessageFromDb(task.getEntityId());
                if (msg != null) {
                    String remoteId = MockRemoteDataSource.getInstance().pushMessage(msg);
                    completeTask(task, remoteId);
                }
            }
        } catch (Exception e) {
            handleTaskFailure(task, e.getMessage());
        }
    }

    private Message getMessageFromDb(String id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM messages WHERE id = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Message(
                        rs.getString("id"),
                        rs.getString("remote_id"),
                        rs.getString("sender"),
                        rs.getString("content"),
                        rs.getString("timestamp"),
                        null,
                        rs.getInt("synced") == 1,
                        rs.getInt("retry_count"),
                        rs.getString("last_error")
                );
            }
        }
        return null;
    }

    private void completeTask(SyncTask task, String remoteId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement ps1 = conn.prepareStatement("UPDATE messages SET remote_id = ?, synced = 1 WHERE id = ?");
                ps1.setString(1, remoteId);
                ps1.setString(2, task.getEntityId());
                ps1.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement("DELETE FROM sync_queue WHERE id = ?");
                ps2.setInt(1, task.getQueueId());
                ps2.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void handleTaskFailure(SyncTask task, String error) {
        int newRetryCount = task.getRetryCount() + 1;
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE sync_queue SET retry_count = ?, last_error = ? WHERE id = ?");
            ps.setInt(1, newRetryCount);
            ps.setString(2, error);
            ps.setInt(3, task.getQueueId());
            ps.executeUpdate();
            
            PreparedStatement psMsg = conn.prepareStatement("UPDATE messages SET retry_count = ?, last_error = ? WHERE id = ?");
            psMsg.setInt(1, newRetryCount);
            psMsg.setString(2, error);
            psMsg.setString(3, task.getEntityId());
            psMsg.executeUpdate();

            if (newRetryCount < MAX_RETRIES) {
                task.setRetryCount(newRetryCount);
                taskQueue.offer(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class SyncTask implements Comparable<SyncTask> {
        private int queueId;
        private String entityType;
        private String entityId;
        private String operation;
        private String payload;
        private int retryCount;
        private long timestamp;

        public SyncTask(int queueId, String entityType, String entityId, String operation, String payload, int retryCount, long timestamp) {
            this.queueId = queueId;
            this.entityType = entityType;
            this.entityId = entityId;
            this.operation = operation;
            this.payload = payload;
            this.retryCount = retryCount;
            this.timestamp = timestamp;
        }

        public int getQueueId() { return queueId; }
        public String getEntityType() { return entityType; }
        public String getEntityId() { return entityId; }
        public String getOperation() { return operation; }
        public int getRetryCount() { return retryCount; }
        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    @Override
        public int compareTo(SyncTask o) {
            return Long.compare(this.timestamp, o.timestamp);
        }
    }

    /**
     * Triggered by WorkManager or other platform-specific schedulers.
     * Synchronizes everything in the queue if online.
     */
    public void syncPendingChanges() {
        if (NetworkService.getInstance().isOnline()) {
            System.out.println("Starting batch sync triggered by platform worker...");
            // The worker thread in startWorkerThread already processes the taskQueue.
            // Here we just ensure all DB tasks are loaded and the worker is processing.
            loadPendingTasksFromDb();
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
