package com.teskalabs.seacat

import android.content.Context
import java.security.cert.CertificateFactory
import java.util.concurrent.Executors

class SeaCat(val context: Context, val title: String, val apiURL: String) {

    companion object {
//        val getURL = "https://keyote.teskalabs.com/get"
//        var enrollURL = "https://keyote.teskalabs.com/enroll"
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val executor = Executors.newSingleThreadScheduledExecutor()
    }

    var identity = Identity(this)
    val peer = Peer(this)

}
