package me.toast.pranksuite;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.util.InputStreamSender;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileTransferListener extends InputStreamSender {

    public FileTransferListener(InputStream inputStream, String name, Action action, Connection connection) {
        super(inputStream, 512);
        this.inputStream = inputStream;
        this.name = name;
        this.action = action;
        this.connection = connection;
    }

    long totalSize;
    long currentSize;
    String name;
    public InputStream inputStream;
    Action action;
    Connection connection;

    @Override
    protected void start () {
        Packets.FileTransferRequest request = new Packets.FileTransferRequest();
        request.name = name;
        request.action = action;
        String path = "./assets/";
        if (action == Action.WALLPAPER)
            path+="backgrounds/";
        if (action == Action.SOUND)
            path+="sounds/";
        if (action == Action.POPUP_HTML)
            path+="html/";
        request.totalSize = new File(path+name).length();
        System.out.println("Sending Change Background Request...");
        connection.sendTCP(request);
    }
    @Override
    protected Object next(byte[] bytes) {
        int length = bytes.length;
        currentSize += length;

        Packets.FileTransferPiece packet = new Packets.FileTransferPiece();
        packet.piece = bytes;

        if (currentSize == totalSize) {
            try {
                inputStream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            totalSize = 0L;
            currentSize = 0L;
        }
        return packet;
    }
}
