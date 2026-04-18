package com.jchat;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Toast;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public class ProfileController {

    @FXML
    private View profileView;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label bioLabel;

    public void initialize() {
        profileView.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> 
                    MobileApplication.getInstance().switchView(Main.MAIN_VIEW)));
                appBar.setTitleText("Profile");
                appBar.getActionItems().clear();
            }
        });

        // Mock data for now
        usernameLabel.setText("John Doe");
        emailLabel.setText("john.doe@example.com");
        bioLabel.setText("Passionate developer and JChat enthusiast. Love building cross-platform apps!");
        
        // Circular avatar
        Circle clip = new Circle(60, 60, 60);
        profileImageView.setClip(clip);
        profileImageView.setImage(new Image("https://ui-avatars.com/api/?name=John+Doe&size=120&background=4f46e5&color=fff", true));
    }

    @FXML
    private void onEditProfile() {
        new Toast("Profile editing is not yet implemented.").show();
        System.out.println("Edit profile clicked");
    }

    @FXML
    private void onLogout() {
        new Toast("Logging out...").show();
        MobileApplication.getInstance().switchView(Main.LOGIN_VIEW);
    }
}
