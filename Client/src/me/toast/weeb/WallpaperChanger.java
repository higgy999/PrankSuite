package me.toast.weeb;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

import java.util.*;

public class WallpaperChanger {
    /*public static native int SystemParametersInfo(int uiAction,int uiParam,String pvParam,int fWinIni);
    static
    {
        System.loadLibrary("user32");
    }

    public static int Change(String path)
    {
        return SystemParametersInfo(20, 0, path, 0);
    }*/

    public static interface User32 extends Library {
        User32 INSTANCE = (User32) Native.loadLibrary("user32",User32.class, W32APIOptions.DEFAULT_OPTIONS);
        boolean SystemParametersInfo (int one, int two, String s ,int three);
    }
    public static void Change(String path) {
        User32.INSTANCE.SystemParametersInfo(0x0014, 0, path, 1);
    }
}