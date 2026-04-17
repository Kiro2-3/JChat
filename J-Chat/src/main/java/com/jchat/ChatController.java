package com.jchat;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.charm.glisten.application.MobileApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatController {
    @FXML
    private ListView<Message> messagesListView;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private AppBar appBar;

    @FXML
    private javafx.scene.layout.VBox mainView;

    private final ObservableList<Message> messages = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Handle Gluon View lifecycle to configure AppBar
        Platform.runLater(() -> {
            if (appBar != null) {
                appBar.setTitleText("Chat");
                appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> 
                    MobileApplication.getInstance().switchView(Main.DASHBOARD_VIEW)));
                appBar.getActionItems().clear();
                appBar.getActionItems().add(MaterialDesignIcon.INFO.button(e -> System.out.println("Info clicked")));
            }
        });

        messagesListView.setItems(messages);
        messagesListView.setCellFactory(list -> new ListCell<>() {
            private final HBox content;
            private final ImageView avatar;
            private final VBox vbox;
            private final Text sender;
            private final Text message;
            private final Text timestamp;
            private final Text syncStatus;
            {
                avatar = new ImageView();
                avatar.setFitWidth(36);
                avatar.setFitHeight(36);
                avatar.setStyle("-fx-background-radius: 18; -fx-effect: dropshadow(gaussian, #e0e0e0, 4, 0.2, 0, 1);");
                sender = new Text();
                sender.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                message = new Text();
                message.setStyle("-fx-font-size: 16px;");
                timestamp = new Text();
                timestamp.setStyle("-fx-font-size: 10px; -fx-fill: #888;");
                syncStatus = new Text();
                syncStatus.setStyle("-fx-font-size: 10px;");
                HBox footer = new HBox(timestamp, syncStatus);
                footer.setSpacing(5);
                vbox = new VBox(sender, message, footer);
                vbox.setSpacing(2);
                content = new HBox(avatar, vbox);
                content.setSpacing(10);
            }
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    sender.setText(item.getSender());
                    message.setText(item.getContent());
                    timestamp.setText(item.getTimestamp());
                    
                    if (item.isSynced()) {
                        syncStatus.setText("✓");
                        syncStatus.setFill(javafx.scene.paint.Color.GREEN);
                        message.setOpacity(1.0);
                        setTooltip(null);
                    } else if (item.getRetryCount() >= 3) { // Use the same MAX_RETRIES constant logic
                        syncStatus.setText("⚠");
                        syncStatus.setFill(javafx.scene.paint.Color.RED);
                        message.setOpacity(0.5);
                        setTooltip(new javafx.scene.control.Tooltip("Failed to sync: " + item.getLastError()));
                    } else {
                        syncStatus.setText("⌛");
                        syncStatus.setFill(javafx.scene.paint.Color.GRAY);
                        message.setOpacity(0.6);
                        setTooltip(null);
                    }
                    
                    String avatarUrl = item.getAvatarUrl();
                    if (avatarUrl != null && !avatarUrl.isBlank()) {
                        avatar.setImage(new Image(avatarUrl, true));
                    } else {
                        avatar.setImage(new Image("https://ui-avatars.com/api/?name=" + item.getSender() + "&background=random", true));
                    }
                    setGraphic(content);
                }
            }
        });
        loadMessages();
    }

    @FXML
    private void onSend() {
        String content = messageField.getText();
        if (content == null || content.isBlank()) return;
        messageField.clear();
        new Thread(() -> {
            MessageRepository.getInstance().sendMessage(content);
            Platform.runLater(this::loadMessages);
        }).start();
    }

    private void loadMessages() {
        new Thread(() -> {
            ObservableList<Message> loaded = MessageRepository.getInstance().getMessages();
            Platform.runLater(() -> {
                messages.setAll(loaded);
                messagesListView.scrollTo(messages.size() - 1);
            });
        }).start();
    }
}
