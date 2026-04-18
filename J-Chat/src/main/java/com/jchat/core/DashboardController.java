package com.jchat.core;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Toast;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.charm.glisten.control.Dialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class DashboardController {

    @FXML
    private View dashboardView;

    @FXML
    private ListView<Contact> contactListView;

    @FXML
    private HBox connectivityBar;

    @FXML
    private TextField searchField;

    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();

    private void loadContacts() {
        new Thread(() -> {
            ObservableList<Contact> loaded = ContactRepository.getInstance().getContacts();
            Platform.runLater(() -> {
                contacts.setAll(loaded);
            });
        }).start();
    }

    public void initialize() {
        // Connectivity Bar logic
        if (connectivityBar != null) {
            connectivityBar.visibleProperty().bind(NetworkService.getInstance().onlineProperty().not());
            connectivityBar.managedProperty().bind(connectivityBar.visibleProperty());
        }

        // Search Bar logic: Change prompt based on offline status
        if (searchField != null) {
            searchField.promptTextProperty().bind(
                javafx.beans.binding.Bindings.when(NetworkService.getInstance().onlineProperty())
                .then("Search messages...")
                .otherwise("Searching local messages only")
            );
        }

        // Initialize Services
        SyncService.getInstance().setConflictListener(this::onConflictDetected);

        dashboardView.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> System.out.println("Menu clicked")));
                appBar.setTitleText("Messages");
                appBar.getActionItems().clear();
                appBar.getActionItems().add(MaterialDesignIcon.ACCOUNT_CIRCLE.button(e -> 
                    MobileApplication.getInstance().switchView(Main.PROFILE_VIEW)));
                
                // Toggle for online/offline mode
                appBar.getActionItems().add(MaterialDesignIcon.WIFI.button(e -> {
                    boolean nowOnline = !NetworkService.getInstance().isOnline();
                    NetworkService.getInstance().setOnline(nowOnline);
                    Toast toast = new Toast(nowOnline ? "Back Online" : "Working Offline");
                    toast.show();
                    System.out.println("Online: " + nowOnline);
                }));

                // Marketplace navigation
                appBar.getActionItems().add(MaterialDesignIcon.SHOPPING_CART.button(e -> 
                    MobileApplication.getInstance().switchView(Main.MARKETPLACE_VIEW)));

                // Button to trigger a mock conflict for testing
                appBar.getActionItems().add(MaterialDesignIcon.REPORT.button(e -> {
                    Contact c = new Contact("1", "Alice Conflict", "Simulated Conflict", "Online", false, 1);
                    ContactRepository.getInstance().updateContact(c);
                }));
            }
        });

        loadContacts();
        contactListView.setItems(contacts);
        contactListView.setCellFactory(lv -> new ListCell<>() {
            private final HBox content;
            private final ImageView avatar;
            private final VBox textContainer;
            private final Text nameText;
            private final Text lastMsgText;
            private final Circle statusCircle;

            {
                avatar = new ImageView();
                avatar.setFitWidth(50);
                avatar.setFitHeight(50);
                Circle clip = new Circle(25, 25, 25);
                avatar.setClip(clip);
                nameText = new Text();
                nameText.getStyleClass().add("contact-name");
                lastMsgText = new Text();
                lastMsgText.getStyleClass().add("contact-last-msg");
                statusCircle = new Circle(6);
                statusCircle.getStyleClass().add("status-indicator");
                textContainer = new VBox(nameText, lastMsgText);
                textContainer.setSpacing(4);
                content = new HBox(avatar, textContainer, new javafx.scene.layout.Region(), statusCircle);
                content.setSpacing(15);
                content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                HBox.setHgrow(content.getChildren().get(2), javafx.scene.layout.Priority.ALWAYS);
                content.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));
            }

            @Override
            protected void updateItem(Contact item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    nameText.setText(item.getName() + (item.isSynced() ? "" : " ⌛"));
                    lastMsgText.setText(item.getLastMessage());
                    avatar.setImage(new Image(item.getAvatarUrl(), true));
                    statusCircle.setFill(switch (item.getStatus()) {
                        case "Online" -> javafx.scene.paint.Color.valueOf("#22c55e");
                        case "Busy" -> javafx.scene.paint.Color.valueOf("#ef4444");
                        default -> javafx.scene.paint.Color.valueOf("#9ca3af");
                    });
                    setGraphic(content);
                }
            }
        });

        contactListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                MobileApplication.getInstance().switchView(Main.MAIN_VIEW);
            }
        });
    }

    private void onConflictDetected(SyncService.SyncTask task, Contact local, Contact server) {
        Platform.runLater(() -> showConflictDialog(task, local, server));
    }

    private void showConflictDialog(SyncService.SyncTask task, Contact local, Contact server) {
        Dialog<Button> dialog = new Dialog<>();
        dialog.setTitleText("Merge Conflict Detected");
        
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(10));
        content.getChildren().add(new Label("Data on the server has changed. Choose which version to keep:"));
        
        HBox comparison = new HBox(20);
        
        VBox localBox = new VBox(5, new Label("MY VERSION"), new Label("Name: " + local.getName()));
        localBox.setStyle("-fx-background-color: #f0fdf4; -fx-padding: 10; -fx-border-color: #22c55e;");
        
        VBox serverBox = new VBox(5, new Label("SERVER VERSION"), new Label("Name: " + server.getName()));
        serverBox.setStyle("-fx-background-color: #fef2f2; -fx-padding: 10; -fx-border-color: #ef4444;");
        
        comparison.getChildren().addAll(localBox, serverBox);
        content.getChildren().add(comparison);
        
        dialog.setContent(content);
        
        Button keepLocal = new Button("Keep My Version");
        keepLocal.setOnAction(e -> {
            // Keep local version, but increment version to server's version + 1 to 'win' the next sync
            Contact resolved = new Contact(local.getId(), local.getName(), local.getLastMessage(), local.getStatus(), false, server.getVersion() + 1);
            SyncService.getInstance().resolveConflict(task, resolved);
            dialog.hide();
            loadContacts();
        });
        
        Button useServer = new Button("Use Server Version");
        useServer.setOnAction(e -> {
            SyncService.getInstance().resolveConflict(task, server);
            dialog.hide();
            loadContacts();
        });
        
        dialog.getButtons().addAll(keepLocal, useServer);
        dialog.showAndWait();
    }
}
