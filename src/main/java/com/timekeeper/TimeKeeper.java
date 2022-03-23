package com.timekeeper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class TimeKeeper extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Tray tray = Tray.getInstance();
        FXMLLoader fxmlLoader = new FXMLLoader(TimeKeeper.class.getResource("tkWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.setFill(Color.TRANSPARENT);
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() - 265 - 20);
        stage.setY(primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight() - 350);
        stage.setWidth(265);
        stage.setHeight(350);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Tardis");
        stage.setScene(scene);

        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {if (!newValue) stage.hide();});
        Platform.setImplicitExit(false);
        System.out.println(getClass().getResource("greenIcon.png"));
        stage.getIcons().add(new javafx.scene.image.Image(String.valueOf(TimeKeeper.class.getResource("icon.png"))));
        stage.requestFocus();
        stage.show();

        tray.getTrayIcon().addActionListener(e -> Platform.runLater(() -> {
            stage.requestFocus();
            stage.show();
        }));
    }

    public static void main(String[] args) {
        launch();
    }
}