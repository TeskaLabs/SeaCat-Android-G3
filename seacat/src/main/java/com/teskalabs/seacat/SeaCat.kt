package com.teskalabs.seacat

import android.content.Context
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.net.Socket
import java.security.KeyStore
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509KeyManager

open class SeaCat(
    internal val context: Context,
    internal val apiURL: String,
    internal val controller: Controller = Controller()
) {

    companion object {
        internal val certificateFactory = CertificateFactory.getInstance("X.509")
        internal val executor = Executors.newSingleThreadScheduledExecutor()

        val TAG = "SeaCat"

        const val CATEGORY_SEACAT = "com.teskalabs.seacat.intent.category.SEACAT"
        const val ACTION_IDENTITY_ENROLLED = "com.teskalabs.seacat.intent.action.IDENTITY_ENROLLED"
        const val ACTION_IDENTITY_REVOKED = "com.teskalabs.seacat.intent.action.IDENTITY_REVOKED"
    }

    val broadcastManager = LocalBroadcastManager.getInstance(context)

    val identity = Identity(this)
    val peers = PeerProvider(this)

    /** Array of trust managers */
    val trustManagers : Array<TrustManager>

    init {

        // Initialize the identity
        // The load has to happen in the synchronous way so that we indicate consistently if the identity is usable or nor
        if (identity.load()) {
            identity.verify()
        } else {
            executor.submit(Callable {
                identity.renew()
            })
        }

        // Initialize array of trust managers
        trustManagers = onCreateTrustManagers()
    }

    val sslContext: SSLContext
        get () {
            val context = SSLContext.getInstance("TLSv1.2")

            context.init(
                arrayOf(
                    // Identity key manager
                    object : X509KeyManager {
                        val identityString = identity.toString()
                        init {
                            if (identityString.equals("")) { throw RuntimeException("Identity is not ready") }
                        }

                        override fun getClientAliases(keyType: String?, issuers: Array<out Principal>?): Array<String> {
                            TODO("not implemented")
                        }

                        override fun getServerAliases(keyType: String?, issuers: Array<out Principal>?): Array<String> {
                            TODO("not implemented")
                        }

                        override fun chooseServerAlias(keyType: String?, issuers: Array<out Principal>?, socket: Socket?): String {
                            TODO("not implemented")
                        }

                        override fun chooseClientAlias(keyType: Array<out String>?, issuers: Array<out Principal>?, socket: Socket?): String {
                            return identityString
                        }

                        override fun getCertificateChain(alias: String?): Array<X509Certificate> {
                            assert(identityString.equals(alias))

                            return arrayOf(
                                identity.certificate!! as X509Certificate//,
//                                intermediateCA as X509Certificate
                            )
                        }

                        override fun getPrivateKey(alias: String?): PrivateKey {
                            assert(identityString.equals(alias))
                            return identity.privateKey!!
                        }

                    }

                ),
                trustManagers,
                null
            )

            return context
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
    protected fun onCreateTrustManagers() : Array<TrustManager> {
        val trustManagerFactory : TrustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val trustedStore = KeyStore.getInstance("AndroidCAStore")

        trustedStore.load(null)
        trustManagerFactory.init(trustedStore)

        return trustManagerFactory.trustManagers
    }
}
