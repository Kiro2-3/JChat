package com.jchat;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.mvc.View;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends MobileApplication {
    public static final String MAIN_VIEW = "MainView";
    public static final String LOGIN_VIEW = "LoginView";
    public static final String PROFILE_VIEW = "ProfileView";
    public static final String DASHBOARD_VIEW = "DashboardView";
    public static final String MARKETPLACE_VIEW = "MarketplaceView";

    @Override
    public void init() {
        addViewFactory(LOGIN_VIEW, () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jchat/LoginView.fxml"));
                Parent root = loader.load();
                return new View(root);
            } catch (Exception e) {
                e.printStackTrace();
                return new View();
            }
        });

        addViewFactory(DASHBOARD_VIEW, () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jchat/DashboardView.fxml"));
                Parent root = loader.load();
                return (View) root;
            } catch (Exception e) {
                e.printStackTrace();
                return new View();
            }
        });

        addViewFactory(MARKETPLACE_VIEW, () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jchat/MarketplaceView.fxml"));
                Parent root = loader.load();
                return (View) root;
            } catch (Exception e) {
                e.printStackTrace();
                return new View();
            }
        });

        addViewFactory(MAIN_VIEW, () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jchat/MainView.fxml"));
                Parent root = loader.load();
                return new View(root);
            } catch (Exception e) {
                e.printStackTrace();
                return new View();
            }
        });

        addViewFactory(PROFILE_VIEW, () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jchat/ProfileView.fxml"));
                Parent root = loader.load();
                return (View) root;
            } catch (Exception e) {
                e.printStackTrace();
                return new View();
            }
        });
    }

    @Override
    public void postInit(Scene scene) {
        super.postInit(scene);
        switchView(LOGIN_VIEW);
    }

    public static void main(String[] args) {
        launch(args);
    }
}