package com.teskalabs.seacat

import java.security.KeyStore
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory

open class Controller {

    open fun onInitialEnrollmentRequested(seacat: SeaCat) {
        // You may decide to call seacat.identity.enroll() later
        seacat.identity.enroll()
    }

    open fun onReenrollmentRequested(seacat: SeaCat) {
        // You may decide to call seacat.identity.enroll() later
        seacat.identity.enroll()
    }

    open fun onAction(action: String) {
        /* You may create an Intent here and broadcast it to the rest of the application

        Sample:

        val intent = Intent()
        intent.addCategory("com.example.category.SEACAT")
        intent.action = "com.example.intent.action." + action
        broadcastManager.sendBroadcast(intent)
         */
    }

    /**
     * Called when trust managers array should be initialized.
     *
     *  By default there is only one trust manager
     *  returned that is initialized with the default
     *  Android CA store.
     *
     *  You can override this method and return your own
     *  array of [TrustManager]s to provide trust to certificates
     *  issued by your company or for certificate pinning.
     */
    open fun createTrustManagers() : Array<TrustManager> {
        val trustManagerFactory : TrustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val trustedStore = KeyStore.getInstance("AndroidCAStore")

        trustedStore.load(null)
        trustManagerFactory.init(trustedStore)

        return trustManagerFactory.trustManagers
    }

}