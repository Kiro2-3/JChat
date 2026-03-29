package com.jchat;

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
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS messages (id INTEGER PRIMARY KEY AUTOINCREMENT, sender TEXT, content TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString());
    }
}