package me.toast.weeb;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WeebPopup extends Application {

    Button okButton;
    Label weebBoy;
    public static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        WeebPopup.stage = stage;
        WeebPopup.stage.setTitle("ALERT! WEEB HAS BEEN FOUND!");
        WeebPopup.stage.setMinWidth(300);
        WeebPopup.stage.setAlwaysOnTop(true);
        WeebPopup.stage.setResizable(false);

        weebBoy = new Label("I have caught you in the act of your weebish activities! They must cease immediately.");
        okButton = new Button("OK");
        okButton.setOnAction(e -> WeebPopup.stage.hide());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(weebBoy, okButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        WeebPopup.stage.setScene(scene);
    }
}
