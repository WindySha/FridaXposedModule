package com.wind.frida.xposed;

import android.app.Application;

/**
 * @author Windysha
 */
public class App extends Application {

    static {
//        System.loadLibrary("frida-gadget");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
