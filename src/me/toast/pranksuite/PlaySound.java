package me.toast.pranksuite;

import jaco.mp3.player.MP3Player;

import java.io.File;

public class PlaySound implements Runnable {

    public PlaySound(File file) {
        this.file = file;
        thread = new Thread(this);
        thread.start();
    }

    File file;
    Thread thread;

    @Override
    public void run() {
        try {
            MP3Player mp3Player = new MP3Player(file);
            mp3Player.play();

            while (!mp3Player.isStopped()) {
                Thread.sleep(5000);
            }
            thread.join();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
