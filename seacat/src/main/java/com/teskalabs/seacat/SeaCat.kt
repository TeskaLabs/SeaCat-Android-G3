package com.teskalabs.seacat

import android.content.Context
import android.support.v4.content.LocalBroadcastManager
import java.security.cert.CertificateFactory
import java.util.concurrent.Executors

class SeaCat(val context: Context, val title: String, val apiURL: String) {

    companion object {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val executor = Executors.newSingleThreadScheduledExecutor()
    }

    val broadcastManager = LocalBroadcastManager.getInstance(context)

    var identity = Identity(this)
    val peer = Peer(this)

}
