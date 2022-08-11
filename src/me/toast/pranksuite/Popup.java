package me.toast.pranksuite;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Popup extends Application {

    public static Button BUTTON;
    public static Label MESSAGE;
    public static Scene POPUP;

    public static WebView WEB_VIEW;
    public static WebEngine ENGINE;
    public static Scene HTML;

    public static Stage STAGE;

    @Override
    public void start(Stage stage) {
        Popup.STAGE = stage;
        Popup.STAGE.setTitle("NULL");
        Popup.STAGE.setMinWidth(300);
        Popup.STAGE.setAlwaysOnTop(true);
        Popup.STAGE.setResizable(false);

        MESSAGE = new Label("NULL");
        BUTTON = new Button("NULL");
        BUTTON.setOnAction(e -> Popup.STAGE.hide());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(MESSAGE, BUTTON);
        layout.setAlignment(Pos.CENTER);
        POPUP = new Scene(layout);

        WEB_VIEW = new WebView();
        ENGINE = WEB_VIEW.getEngine();
        HTML = new Scene(WEB_VIEW);
    }
}
