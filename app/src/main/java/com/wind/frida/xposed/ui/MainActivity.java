package com.wind.frida.xposed.ui;

import android.app.Activity;
import android.os.Bundle;

import com.wind.frida.xposed.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
