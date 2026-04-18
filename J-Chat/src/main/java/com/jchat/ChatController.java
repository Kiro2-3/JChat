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
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Toast;
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
    private HBox connectivityBar;

    @FXML
    private javafx.scene.layout.VBox mainView;

    private final ObservableList<Message> messages = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Connectivity Bar logic
        if (connectivityBar != null) {
            connectivityBar.visibleProperty().bind(NetworkService.getInstance().onlineProperty().not());
            connectivityBar.managedProperty().bind(connectivityBar.visibleProperty());
        }

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
            private final javafx.scene.control.Label syncIcon;
            private final javafx.scene.control.Button retryBtn;
            private final ImageView mediaView;
            private SyncStatus lastStatus = null;

            {
                avatar = new ImageView();
                avatar.setFitWidth(36);
                avatar.setFitHeight(36);
                avatar.setStyle("-fx-background-radius: 18; -fx-effect: dropshadow(gaussian, #e0e0e0, 4, 0.2, 0, 1);");
                
                sender = new Text();
                sender.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-fill: #555;");
                
                message = new Text();
                message.setStyle("-fx-font-size: 15px;");
                message.setWrappingWidth(250);

                mediaView = new ImageView();
                mediaView.setFitWidth(200);
                mediaView.setPreserveRatio(true);
                mediaView.setStyle("-fx-background-radius: 10;");

                timestamp = new Text();
                timestamp.setStyle("-fx-font-size: 10px; -fx-fill: #999;");
                
                syncIcon = new javafx.scene.control.Label();
                syncIcon.setStyle("-fx-padding: 0 0 0 5;");

                retryBtn = new javafx.scene.control.Button("Retry");
                retryBtn.getStyleClass().add("retry-button");
                retryBtn.setVisible(false);
                retryBtn.setManaged(false);

                HBox footer = new HBox(timestamp, syncIcon, retryBtn);
                footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                footer.setSpacing(5);

                VBox bubble = new VBox(sender, mediaView, message, footer);
                bubble.setSpacing(5);
                bubble.setPadding(new javafx.geometry.Insets(8, 12, 8, 12));
                bubble.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 15;");

                vbox = new VBox(bubble);
                content = new HBox(avatar, vbox);
                content.setSpacing(10);
                content.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
            }

            private void playPopAnimation() {
                ScaleTransition st = new ScaleTransition(Duration.millis(300), syncIcon);
                st.setFromX(0.5); st.setFromY(0.5);
                st.setToX(1.2); st.setToY(1.2);
                st.setCycleCount(2);
                st.setAutoReverse(true);
                st.play();
            }

            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setTooltip(null);
                    lastStatus = null;
                } else {
                    sender.setText(item.getSender());
                    message.setText(item.getContent());
                    timestamp.setText(item.getTimestamp());
                    
                    SyncStatus status = item.getSyncStatus();
                    
                    // Micro-interaction: Pop animation on status change to SENT
                    if (lastStatus == SyncStatus.PENDING && status == SyncStatus.SENT) {
                        playPopAnimation();
                    }
                    lastStatus = status;

                    syncIcon.setGraphic(null);
                    retryBtn.setVisible(false);
                    retryBtn.setManaged(false);
                    
                    switch (status) {
                        case PENDING:
                            syncIcon.setGraphic(MaterialDesignIcon.SCHEDULE.graphic());
                            syncIcon.getGraphic().setStyle("-fx-fill: #999; -fx-font-size: 12px;");
                            vbox.setOpacity(0.6);
                            setTooltip(null);
                            break;
                        case SENT:
                            syncIcon.setGraphic(MaterialDesignIcon.DONE.graphic());
                            syncIcon.getGraphic().setStyle("-fx-fill: #4CAF50; -fx-font-size: 12px;");
                            vbox.setOpacity(1.0);
                            setTooltip(null);
                            break;
                        case DELIVERED:
                            syncIcon.setGraphic(MaterialDesignIcon.DONE_ALL.graphic());
                            syncIcon.getGraphic().setStyle("-fx-fill: #2196F3; -fx-font-size: 12px;");
                            vbox.setOpacity(1.0);
                            setTooltip(null);
                            break;
                        case FAILED:
                            syncIcon.setGraphic(MaterialDesignIcon.REPORT_PROBLEM.graphic());
                            syncIcon.getGraphic().setStyle("-fx-fill: #F44336; -fx-font-size: 12px;");
                            vbox.setOpacity(0.5);
                            setTooltip(new javafx.scene.control.Tooltip("Failed: " + item.getLastError()));
                            retryBtn.setVisible(true);
                            retryBtn.setManaged(true);
                            retryBtn.setOnAction(e -> retrySync(item.getId()));
                            break;
                    }

                    // Media Logic: Cloud-only vs Local
                    if (item.getMediaUrl() != null) {
                        mediaView.setVisible(true);
                        mediaView.setManaged(true);
                        if (item.isMediaDownloaded()) {
                            mediaView.setImage(new Image(item.getMediaUrl(), true));
                            mediaView.setOpacity(1.0);
                        } else {
                            // Cloud-only design: Blurred/Greyed out with cloud icon
                            mediaView.setImage(new Image("https://via.placeholder.com/200x120?text=Cloud+Image", true));
                            mediaView.setOpacity(0.4);
                        }
                    } else {
                        mediaView.setVisible(false);
                        mediaView.setManaged(false);
                    }
                    
                    // Alignment logic: "Me" messages on the right
                    if ("Me".equals(item.getSender())) {
                        content.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                        content.getChildren().setAll(vbox, avatar);
                        vbox.getChildren().get(0).setStyle("-fx-background-color: #DCF8C6; -fx-background-radius: 15;");
                    } else {
                        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        content.getChildren().setAll(avatar, vbox);
                        vbox.getChildren().get(0).setStyle("-fx-background-color: #ffffff; -fx-background-radius: 15;");
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

    private void retrySync(String messageId) {
        new Toast("Retrying synchronization...").show();
        new Thread(() -> {
            SyncService.getInstance().retryTask(messageId);
            Platform.runLater(this::loadMessages);
        }).start();
    }

    @FXML
    private void onSend() {
        String content = messageField.getText();
        if (content == null || content.isBlank()) {
            new Toast("Cannot send empty message").show();
            return;
        }
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
