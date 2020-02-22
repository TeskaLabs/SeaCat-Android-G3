package com.teskalabs.seacat.demo

import android.app.Application
import com.teskalabs.seacat.SeaCat

class KeyoteDemoApp : Application() {

    lateinit var seacat: SeaCat
    val seaCatController = CustomSeaCatController()

    override fun onCreate() {
        super.onCreate()

        seacat = SeaCat(this, "https://pki.seacat.io/seacat-demo/seacat", seaCatController)
    }

}
