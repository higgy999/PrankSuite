package me.toast.pranksuite;

import com.esotericsoftware.kryo.Kryo;

public class Packets {
    public static class OpenWindowsRequest {}
    public static class OpenWindowsStartResponse {}
    public static class OpenWindowsResponse {public String openWindow;}
    public static class OpenWindowsFinalResponse {}
    public static class FileTransferRequest {public Action action; public String name; public long totalSize;}
    public static class FileTransferPiece {public byte[] piece;}
    public static class FileTransferFinish {}
    public static class TriggerPopup {public String title; public String message; public String button;}
    public static class TriggerHTMLPopup {}
    public static class CloseWindow {public String nameOfWindow;}

    public static void RegisterPackets(Kryo kryo) {
        kryo.register(OpenWindowsRequest.class);
        kryo.register(OpenWindowsStartResponse.class);
        kryo.register(OpenWindowsResponse.class);
        kryo.register(OpenWindowsFinalResponse.class);
        kryo.register(FileTransferFinish.class);
        kryo.register(FileTransferRequest.class);
        kryo.register(byte[].class);
        kryo.register(Action.class);
        kryo.register(FileTransferPiece.class);
        kryo.register(TriggerPopup.class);
        kryo.register(CloseWindow.class);
    }
}
