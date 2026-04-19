package com.reloved.marketplace;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Toast;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class MarketplaceController {

    @FXML
    private View marketplaceView;

    @FXML
    private ListView<Item> itemListView;

    private final ObservableList<Item> items = FXCollections.observableArrayList();

    public void initialize() {
        marketplaceView.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> 
                    MobileApplication.getInstance().switchView(Main.DASHBOARD_VIEW)));
                appBar.setTitleText("Marketplace");
                appBar.getActionItems().clear();
            }
        });

        loadItems();
        itemListView.setItems(items);
        itemListView.setCellFactory(lv -> new ListCell<>() {
            private final VBox content = new VBox();
            private final Text title = new Text();
            private final Text description = new Text();
            private final Text type = new Text();

            {
                content.getChildren().addAll(title, description, type);
            }

            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    title.setText(item.getTitle());
                    description.setText(item.getDescription());
                    type.setText("[" + item.getType() + "]");
                    setGraphic(content);
                }
            }
        });
    }

    // Helper method to safely add an item
    public void postItem(String title, String description, String type) {
        if (title == null || title.isBlank() || description == null || description.isBlank()) {
            new Toast("Title and description are required").show();
            return;
        }
        
        if (ItemRepository.getInstance().addItem(title, description, type)) {
            new Toast("Item posted successfully!").show();
            loadItems();
        } else {
            new Toast("Failed to post item").show();
        }
    }

    private void loadItems() {
        try {
            items.setAll(ItemRepository.getInstance().getItems());
        } catch (Exception e) {
            new Toast("Failed to load items from marketplace.").show();
        }
    }
}
