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

    @Override
    public void init() {
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
    }

    public static void main(String[] args) {
        launch(args);
    }
}