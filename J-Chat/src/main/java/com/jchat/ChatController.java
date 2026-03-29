package com.jchat;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import com.gluonhq.charm.glisten.control.AppBar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatController {
    @FXML
    private ListView<String> messagesListView;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private AppBar appBar;

    private final ObservableList<String> messages = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        messagesListView.setItems(messages);
        appBar.setTitleText("JChat");
        loadMessages();
    }

    @FXML
    private void onSend() {
        String content = messageField.getText();
        if (content == null || content.isBlank()) return;
        messageField.clear();
        new Thread(() -> {
            try (Connection conn = DatabaseManager.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO messages (sender, content) VALUES (?, ?)");
                ps.setString(1, "Me");
                ps.setString(2, content);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Platform.runLater(this::loadMessages);
        }).start();
    }

    private void loadMessages() {
        new Thread(() -> {
            ObservableList<String> loaded = FXCollections.observableArrayList();
            try (Connection conn = DatabaseManager.getConnection()) {
                ResultSet rs = conn.createStatement().executeQuery("SELECT sender, content, timestamp FROM messages ORDER BY timestamp ASC");
                while (rs.next()) {
                    String msg = String.format("[%s] %s: %s", rs.getString("timestamp"), rs.getString("sender"), rs.getString("content"));
                    loaded.add(msg);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                messages.setAll(loaded);
                messagesListView.scrollTo(messages.size() - 1);
            });
        }).start();
    }
}
