package com.jchat;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
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

public class DashboardController {

    @FXML
    private View dashboardView;

    @FXML
    private ListView<Contact> contactListView;

    @FXML
    private HBox connectivityBar;

    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();

    private void loadContacts() {
        new Thread(() -> {
            ObservableList<Contact> loaded = ContactRepository.getInstance().getContacts();
            javafx.application.Platform.runLater(() -> {
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

        // Initialize Services
        SyncService.getInstance(); 

        dashboardView.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> System.out.println("Menu clicked")));
                appBar.setTitleText("Messages");
                appBar.getActionItems().clear();
                appBar.getActionItems().add(MaterialDesignIcon.ACCOUNT_CIRCLE.button(e -> 
                    MobileApplication.getInstance().switchView(Main.PROFILE_VIEW)));
                
                // Add a toggle for online/offline mode for testing
                appBar.getActionItems().add(MaterialDesignIcon.WIFI.button(e -> {
                    NetworkService.getInstance().setOnline(!NetworkService.getInstance().isOnline());
                    System.out.println("Online: " + NetworkService.getInstance().isOnline());
                }));
            }
        });

        // Load contacts from repository
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
                    nameText.setText(item.getName());
                    lastMsgText.setText(item.getLastMessage());
                    avatar.setImage(new Image(item.getAvatarUrl(), true));
                    
                    // Status color logic
                    statusCircle.setFill(switch (item.getStatus()) {
                        case "Online" -> javafx.scene.paint.Color.valueOf("#22c55e");
                        case "Busy" -> javafx.scene.paint.Color.valueOf("#ef4444");
                        default -> javafx.scene.paint.Color.valueOf("#9ca3af");
                    });

                    setGraphic(content);
                }
            }
        });

        // Navigate to chat on click
        contactListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                MobileApplication.getInstance().switchView(Main.MAIN_VIEW);
                // In a real app, you'd pass the contact ID to the chat controller
            }
        });
    }
}
