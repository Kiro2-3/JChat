package com.jchat.core;

import com.gluonhq.charm.glisten.control.Toast;
import javafx.application.Platform;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ExecutorService;

public class SyncService {
    private static final SyncService instance = new SyncService();
    private final PriorityBlockingQueue<SyncTask> taskQueue = new PriorityBlockingQueue<>();
    private final ExecutorService workerExecutor = Executors.newSingleThreadExecutor();
    private static final int MAX_RETRIES = 3;

    public interface ConflictResolutionListener {
        void onConflict(SyncTask task, Contact localData, Contact serverData);
    }

    private ConflictResolutionListener conflictListener;

    public void setConflictListener(ConflictResolutionListener listener) {
        this.conflictListener = listener;
    }

    private SyncService() {
        loadPendingTasksFromDb();
        startWorkerThread();
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
                    SyncTask task = taskQueue.take();
                    if (NetworkService.getInstance().isOnline()) {
                        processTask(task);
                    } else {
                        taskQueue.put(task);
                        Thread.sleep(5000);
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
                        0,
                        System.currentTimeMillis()
                );
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
            } else if ("CONTACT".equals(task.getEntityType()) && "UPDATE".equals(task.getOperation())) {
                Contact contact = getContactFromDb(task.getEntityId());
                if (contact != null) {
                    try {
                        MockRemoteDataSource.getInstance().updateContact(contact);
                        completeContactTask(task);
                    } catch (ConflictException ce) {
                        if (conflictListener != null) {
                            Platform.runLater(() -> conflictListener.onConflict(task, contact, (Contact) ce.getServerVersion()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            handleTaskFailure(task, e.getMessage());
        }
    }

    private Contact getContactFromDb(String id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM contacts WHERE id = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Contact(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("lastMessage"),
                        rs.getString("status"),
                        rs.getInt("synced") == 1,
                        rs.getInt("version")
                );
            }
        }
        return null;
    }

    private void completeContactTask(SyncTask task) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement ps1 = conn.prepareStatement("UPDATE contacts SET synced = 1 WHERE id = ?");
                ps1.setString(1, task.getEntityId());
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

    public void resolveConflict(SyncTask task, Contact resolvedContact) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE contacts SET name = ?, lastMessage = ?, status = ?, version = ?, synced = 0 WHERE id = ?");
            ps.setString(1, resolvedContact.getName());
            ps.setString(2, resolvedContact.getLastMessage());
            ps.setString(3, resolvedContact.getStatus());
            ps.setInt(4, resolvedContact.getVersion());
            ps.setString(5, resolvedContact.getId());
            ps.executeUpdate();
            
            task.setRetryCount(0);
            addTask(task);
        } catch (SQLException e) {
            e.printStackTrace();
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
        Platform.runLater(() -> {
            new Toast("Sync failed for " + task.getEntityType().toLowerCase() + ": " + error).show();
        });
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE sync_queue SET retry_count = ?, last_error = ? WHERE id = ?");
            ps.setInt(1, newRetryCount);
            ps.setString(2, error);
            ps.setInt(3, task.getQueueId());
            ps.executeUpdate();
            
            if (task.getEntityType().equals("MESSAGE")) {
                PreparedStatement psMsg = conn.prepareStatement("UPDATE messages SET retry_count = ?, last_error = ? WHERE id = ?");
                psMsg.setInt(1, newRetryCount);
                psMsg.setString(2, error);
                psMsg.setString(3, task.getEntityId());
                psMsg.executeUpdate();
            } else if (task.getEntityType().equals("CONTACT")) {
                PreparedStatement psContact = conn.prepareStatement("UPDATE contacts SET retry_count = ?, last_error = ? WHERE id = ?");
                psContact.setInt(1, newRetryCount);
                psContact.setString(2, error);
                psContact.setString(3, task.getEntityId());
                psContact.executeUpdate();
            }

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

    public void syncPendingChanges() {
        if (NetworkService.getInstance().isOnline()) {
            loadPendingTasksFromDb();
        }
    }

    private void syncContacts() {
        try {
            var remoteContacts = MockRemoteDataSource.getInstance().fetchContacts();
            try (Connection conn = DatabaseManager.getConnection()) {
                for (Contact c : remoteContacts) {
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT OR REPLACE INTO contacts (id, name, lastMessage, status, avatarUrl, synced, version) VALUES (?, ?, ?, ?, ?, 1, ?)");
                    ps.setString(1, c.getId());
                    ps.setString(2, c.getName());
                    ps.setString(3, c.getLastMessage());
                    ps.setString(4, c.getStatus());
                    ps.setString(5, c.getAvatarUrl());
                    ps.setInt(6, c.getVersion());
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to sync contacts: " + e.getMessage());
        }
    }
}
