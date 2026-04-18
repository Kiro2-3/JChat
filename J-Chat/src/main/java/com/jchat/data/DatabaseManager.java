package com.jchat.data;

import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_NAME = "jchat.db";
    private static Path dbPath;

    static {
        dbPath = Services.get(StorageService.class)
                .flatMap(StorageService::getPrivateStorage)
                .map(java.io.File::toPath)
                .orElseGet(() -> Path.of(System.getProperty("user.home")))
                .resolve(DB_NAME);
        initializeDatabase();
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                // Update messages table - Ensure columns exist for migration
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS messages (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "remote_id TEXT, " +
                        "sender TEXT, " +
                        "content TEXT, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "synced INTEGER DEFAULT 0)");
                
                try {
                    stmt.executeUpdate("ALTER TABLE messages ADD COLUMN remote_id TEXT");
                } catch (SQLException ignored) {} 
                try {
                    stmt.executeUpdate("ALTER TABLE messages ADD COLUMN synced INTEGER DEFAULT 0");
                } catch (SQLException ignored) {}
                try {
                    stmt.executeUpdate("ALTER TABLE messages ADD COLUMN retry_count INTEGER DEFAULT 0");
                } catch (SQLException ignored) {}
                try {
                    stmt.executeUpdate("ALTER TABLE messages ADD COLUMN last_error TEXT");
                } catch (SQLException ignored) {}

                // Create contacts table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS contacts (" +
                        "id TEXT PRIMARY KEY, " +
                        "name TEXT, " +
                        "lastMessage TEXT, " +
                        "status TEXT, " +
                        "avatarUrl TEXT, " +
                        "synced INTEGER DEFAULT 1, " +
                        "version INTEGER DEFAULT 1, " +
                        "retry_count INTEGER DEFAULT 0, " +
                        "last_error TEXT)");

                try {
                    stmt.executeUpdate("ALTER TABLE contacts ADD COLUMN version INTEGER DEFAULT 1");
                } catch (SQLException ignored) {}
                try {
                    stmt.executeUpdate("ALTER TABLE contacts ADD COLUMN retry_count INTEGER DEFAULT 0");
                } catch (SQLException ignored) {}
                try {
                    stmt.executeUpdate("ALTER TABLE contacts ADD COLUMN last_error TEXT");
                } catch (SQLException ignored) {}

                // Create sync_queue table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS sync_queue (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "entity_type TEXT, " +
                        "entity_id TEXT, " +
                        "operation TEXT, " +
                        "payload TEXT, " +
                        "retry_count INTEGER DEFAULT 0, " +
                        "last_error TEXT, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

                // Create items table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT, " +
                        "description TEXT, " +
                        "type TEXT, " + // "SELL" or "WANTED"
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString());
    }
}
