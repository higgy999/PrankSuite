package me.toast.weeb;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WeebPopup extends Application {

    public static Button BUTTON;
    public static Label MESSAGE;
    public static Stage STAGE;

    @Override
    public void start(Stage stage) throws Exception {
        WeebPopup.STAGE = stage;
        WeebPopup.STAGE.setTitle("NULL");
        WeebPopup.STAGE.setMinWidth(300);
        WeebPopup.STAGE.setAlwaysOnTop(true);
        WeebPopup.STAGE.setResizable(false);

        MESSAGE = new Label("NULL");
        BUTTON = new Button("NULL");
        BUTTON.setOnAction(e -> WeebPopup.STAGE.hide());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(MESSAGE, BUTTON);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        WeebPopup.STAGE.setScene(scene);
    }
}
