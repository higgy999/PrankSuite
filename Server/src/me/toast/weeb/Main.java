package me.toast.weeb;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    public static Server SERVER;

    public static Label openWindowLabel;
    public static Label connectedIpLabel;

    public static Button askForWindows;
    public static Button changeBackground;
    public static Button createPopUp;
    public static TextField windowName;
    public static Button closeWindow;
    public static TextArea openWindows;

    public static TextField clientIP;
    public static TextArea clientList;

    public static List<String> fetchedWindows = new ArrayList<>();
    public static HashMap<Integer, String> connectedClients = new HashMap<>();

    public static Listener LISTENER = new Listener() {
        public void connected(Connection connection) {
            connectedClients.put(connection.getID(), connection.getRemoteAddressTCP().getHostString());
            StringBuilder sb = new StringBuilder();
            for (int id: connectedClients.keySet()) {
                sb.append(connectedClients.get(id)).append("\n");
            }
            clientList.setText(sb.toString());
        }
        public void disconnected(Connection connection) {
            connectedClients.remove(connection.getID());
            StringBuilder sb = new StringBuilder();
            for (int id: connectedClients.keySet()) {
                sb.append(connectedClients.get(id)).append("\n");
            }
            clientList.setText(sb.toString());
        }
        public void received(Connection connection, Object object) {
            if (object instanceof Packets.OpenWindowsStartResponse) {
                System.out.println("Starting to receive OpenWindowsResponse packets!");
                fetchedWindows.clear();
            }
            if (object instanceof Packets.OpenWindowsResponse response) {
                fetchedWindows.add(response.openWindow);
            }
            if (object instanceof Packets.OpenWindowsFinalResponse) {
                StringBuilder toSubmit = new StringBuilder();
                for (String fetchedWindow : fetchedWindows) {
                    toSubmit.append(fetchedWindow).append("\n");
                }
                openWindows.setText(toSubmit.toString());
                
                Platform.runLater(() -> openWindowLabel.setText("Currently open windows from: " + connection.getRemoteAddressTCP().getHostString()));
            }
        }
    };

    public static void main(String[] args) throws IOException {
        SERVER = new Server();
        Packets.RegisterPackets(SERVER.getKryo());
        Thread thread = new Thread(SERVER); thread.start();
        SERVER.bind(54555);
        SERVER.addListener(LISTENER);

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Weeb Detector - Server");

        askForWindows = new Button("Get Open Windows");
        askForWindows.setOnAction(e -> {
            Packets.OpenWindowsRequest request = new Packets.OpenWindowsRequest();
            System.out.println("Sending Window Request...");
            try { getConnectionFromText(clientIP.getText()).sendTCP(request); } catch (NullPointerException except) {System.out.println("Client IP given was invalid!");}
        });

        changeBackground = new Button("Change Background");
        changeBackground.setOnAction(e -> {
            Packets.ChangeBackground request = new Packets.ChangeBackground();
            System.out.println("Sending Change Background Request...");
            try { getConnectionFromText(clientIP.getText()).sendTCP(request); } catch (NullPointerException except) {System.out.println("Client IP given was invalid!");}
        });

        createPopUp = new Button("Create Popup");
        createPopUp.setOnAction(e -> {
            Packets.TriggerPopup request = new Packets.TriggerPopup();
            System.out.println("Sending Popup Request...");
            try { getConnectionFromText(clientIP.getText()).sendTCP(request); } catch (NullPointerException except) {System.out.println("Client IP given was invalid!");}
        });

        windowName = new TextField();
        windowName.setPromptText("Window Name to Close");
        windowName.setPrefWidth(200);

        closeWindow = new Button("Close Window");
        closeWindow.setOnAction(e -> {
            Packets.CloseWindow request = new Packets.CloseWindow();
            request.nameOfWindow = windowName.getText();
            System.out.println("Sending Close Window Request...");
            try { getConnectionFromText(clientIP.getText()).sendTCP(request); } catch (NullPointerException except) {System.out.println("Client IP given was invalid!");}
        });

        openWindows = new TextArea();
        openWindows.setPrefWidth(500);
        openWindows.setPrefHeight(600);
        openWindows.setEditable(false);

        clientIP = new TextField();
        clientIP.setPromptText("Client IP");
        clientIP.setPrefWidth(200);

        clientList = new TextArea();
        clientList.setPrefWidth(500);
        clientList.setPrefHeight(600);
        clientList.setEditable(false);

        openWindowLabel = new Label("Currently open windows from: ");
        connectedIpLabel = new Label("List of Connected Clients");

        HBox buttonHost = new HBox();
        buttonHost.setAlignment(Pos.TOP_CENTER);
        buttonHost.getChildren().addAll(askForWindows, changeBackground, createPopUp, closeWindow);

        HBox namesHost = new HBox();
        namesHost.setAlignment(Pos.TOP_CENTER);
        namesHost.getChildren().addAll(clientIP, windowName);

        VBox openHost = new VBox();
        openHost.setAlignment(Pos.BOTTOM_RIGHT);
        openHost.getChildren().addAll(openWindowLabel, openWindows);

        VBox clientHost = new VBox();
        clientHost.setAlignment(Pos.BOTTOM_LEFT);
        clientHost.getChildren().addAll(connectedIpLabel, clientList);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(buttonHost);
        borderPane.setCenter(namesHost);
        borderPane.setLeft(clientHost);
        borderPane.setRight(openHost);

        Scene scene = new Scene(borderPane, 1280, 720);
        stage.setScene(scene);
        stage.show();
    }

    public static Connection getConnectionFromText(String text) {
        for (int i = 0; i < SERVER.getConnections().length; i++)
            if(SERVER.getConnections()[i].getRemoteAddressTCP().getHostString().equals(text))
                return SERVER.getConnections()[i];
        return null;
    }

    @Override
    public void stop() throws Exception {
        SERVER.stop();
        super.stop();
    }
}
