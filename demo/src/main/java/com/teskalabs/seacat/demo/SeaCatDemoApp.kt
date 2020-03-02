package com.teskalabs.seacat.demo

import android.app.Application
import com.teskalabs.seacat.SeaCat

class SeaCatDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SeaCat.init(this, "https://pki.seacat.io/seacat-demo/seacat")
    }
}
