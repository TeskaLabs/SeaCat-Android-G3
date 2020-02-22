package com.teskalabs.seacat

import java.security.KeyStore
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory

open class Controller {

    open fun onIntialEnrollmentRequested(seacat: SeaCat) {
        // You may decide to call seacat.identity.enroll() later, when you have more info
        seacat.identity.enroll()
    }

    open fun onReenrollmentRequested(seacat: SeaCat) {
        // You may decide to call seacat.identity.enroll() later, when you have more info
        seacat.identity.enroll()
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