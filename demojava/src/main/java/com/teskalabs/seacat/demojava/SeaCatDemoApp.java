package com.teskalabs.seacat.demojava;

import android.app.Application;

import com.teskalabs.seacat.SeaCat;

public class SeaCatDemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SeaCat.init(this, "https://pki.seacat.io/seacat-demo/seacat");
    }
}
