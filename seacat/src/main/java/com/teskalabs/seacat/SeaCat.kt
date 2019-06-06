package com.teskalabs.seacat

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.security.cert.CertificateFactory
import java.util.concurrent.Executors

class SeaCat(internal val context: Context, internal val title: String, internal val apiURL: String) {

    companion object {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val executor = Executors.newSingleThreadScheduledExecutor()

        const val CATEGORY_SEACAT = "com.teskalabs.seacat.intent.category.SEACAT"
        const val ACTION_IDENTITY_ESTABLISHED = "com.teskalabs.seacat.intent.action.IDENTITY_ESTABLISHED"
        const val ACTION_IDENTITY_RESET = "com.teskalabs.seacat.intent.action.IDENTITY_RESET"
    }

    val identity = Identity(this)
    val peer = Peer(this)

    internal val broadcastManager = LocalBroadcastManager.getInstance(context)
}
