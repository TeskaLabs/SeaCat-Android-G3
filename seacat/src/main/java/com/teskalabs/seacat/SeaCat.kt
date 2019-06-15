package com.teskalabs.seacat

import android.content.Context
import android.support.v4.content.LocalBroadcastManager
import java.security.cert.CertificateFactory
import java.util.concurrent.Executors

class SeaCat(internal val context: Context, internal val title: String, internal val apiURL: String) {

    companion object {
        internal val certificateFactory = CertificateFactory.getInstance("X.509")
        internal val executor = Executors.newSingleThreadScheduledExecutor()

        const val CATEGORY_SEACAT = "com.teskalabs.seacat.intent.category.SEACAT"
        const val ACTION_IDENTITY_ESTABLISHED = "com.teskalabs.seacat.intent.action.IDENTITY_ESTABLISHED"
        const val ACTION_IDENTITY_RESET = "com.teskalabs.seacat.intent.action.IDENTITY_RESET"
    }

    val identity = Identity(this)
    val peer = Peer(this)

    internal val broadcastManager = LocalBroadcastManager.getInstance(context)
}
