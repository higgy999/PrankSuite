package me.toast.weeb;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.esotericsoftware.kryonet.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PSServer extends Application {

    //TODO Redo access levels

    public static Server SERVER;

    public static Button refreshOpenWindows;
    public static Button changeBackground;
    public static Button createPopUp;
    public static Button closeWindow;
    public static ListView<String> openWindows;
    private String selectedWindow;

    public static ListView<String> clientList;
    private String selectedClient;

    public static List<String> fetchedWindows = new ArrayList<>();
    //TODO: Figure out better way getting Connection from IP
    public static HashMap<Integer, String> connectedClients = new HashMap<>();

    public static Listener LISTENER = new Listener() {
        public void connected(Connection connection) {
            connectedClients.put(connection.getID(), connection.getRemoteAddressTCP().getHostString());
             Platform.runLater(() -> clientList.getItems().add(connection.getRemoteAddressTCP().getHostString()));
        }
        public void disconnected(Connection connection) {
            Platform.runLater(() -> {
                clientList.getItems().remove(connectedClients.get(connection.getID()));
                connectedClients.remove(connection.getID());
            });
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
                Platform.runLater(() -> {
                    openWindows.getItems().clear();
                    openWindows.getItems().addAll(fetchedWindows);
                });
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
    public void start(Stage stage) {
        stage.setTitle("Weeb Detector - Server");

        refreshOpenWindows = new Button("Refresh");
        refreshOpenWindows.setOnAction(e -> askForWindows(selectedClient));

        changeBackground = new Button("Change Background");
        changeBackground.setOnAction(e -> {
            Packets.ChangeBackground request = new Packets.ChangeBackground();
            System.out.println("Sending Change Background Request...");
            try { Objects.requireNonNull(getConnectionFromIP(selectedClient)).sendTCP(request); } catch (NullPointerException except) {System.out.println("Client IP given was invalid!");}
        });

        createPopUp = new Button("Create Popup");
        createPopUp.setOnAction(e -> {
            Packets.TriggerPopup request = new Packets.TriggerPopup();
            System.out.println("Sending Popup Request...");
            try { Objects.requireNonNull(getConnectionFromIP(selectedClient)).sendTCP(request); } catch (NullPointerException except) {System.out.println("Client IP given was invalid!");}
        });

        closeWindow = new Button("Close Window");
        closeWindow.setOnAction(e -> {
            if (fetchedWindows.contains(selectedWindow)) {
                Packets.CloseWindow request = new Packets.CloseWindow();
                request.nameOfWindow = selectedWindow;
                System.out.println("Sending Close Window Request...");
                try {
                    Objects.requireNonNull(getConnectionFromIP(selectedClient)).sendTCP(request);
                } catch (NullPointerException except) {
                    System.out.println("Client IP given was invalid!");
                }
                askForWindows(selectedClient);
            }
        });

        openWindows = new ListView<>();
        openWindows.setPrefWidth(500);
        openWindows.setPrefHeight(600);
        openWindows.setEditable(false);
        openWindows.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> selectedWindow = newValue);

        clientList = new ListView<>();
        clientList.setPrefWidth(500);
        clientList.setPrefHeight(600);
        clientList.setEditable(false);
        clientList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            askForWindows(newValue);
            selectedClient = newValue;
        });

        HBox buttonHost = new HBox();
        buttonHost.setAlignment(Pos.TOP_CENTER);
        buttonHost.getChildren().addAll(changeBackground, createPopUp, closeWindow);

        VBox openHost = new VBox();
        openHost.setAlignment(Pos.BOTTOM_LEFT);
        HBox tmp = new HBox();
        tmp.getChildren().addAll(new Label("Open windows:"), refreshOpenWindows);
        openHost.getChildren().addAll(tmp, openWindows);

        VBox clientHost = new VBox();
        clientHost.setAlignment(Pos.BOTTOM_LEFT);
        clientHost.getChildren().addAll(new Label("Connected clients:"), clientList);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(buttonHost);
        borderPane.setLeft(clientHost);
        borderPane.setRight(openHost);

        Scene scene = new Scene(borderPane, 1280, 720);
        stage.setScene(scene);
        stage.show();
    }

    public void askForWindows(String ip) {
        Packets.OpenWindowsRequest request = new Packets.OpenWindowsRequest();
        System.out.println("Sending Window Request...");
        try { Objects.requireNonNull(getConnectionFromIP(ip)).sendTCP(request); } catch (NullPointerException except) {System.out.println("IP given was invalid!");}
    }

    public static Connection getConnectionFromIP(String ip) {
        for (int i = 0; i < SERVER.getConnections().length; i++)
            if(SERVER.getConnections()[i].getRemoteAddressTCP().getHostString().equals(ip))
                return SERVER.getConnections()[i];
        return null;
    }

    @Override
    public void stop() throws Exception {
        SERVER.stop();
        super.stop();
    }
}
