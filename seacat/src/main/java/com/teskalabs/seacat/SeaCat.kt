package com.teskalabs.seacat

import android.content.Context
import android.content.SharedPreferences
import java.net.Socket
import java.net.URL
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.SSLContext
import javax.net.ssl.X509KeyManager
import javax.net.ssl.X509TrustManager

class SeaCat(
    internal val context: Context,
    private val apiURL: String,
    internal val controller: Controller = Controller()
) {

    companion object {

        @JvmStatic
        val TAG = "SeaCat"

        const val SHARED_PREF_KEY = "SEACAT_SHARED_PREFS"

        const val ACTION_IDENTITY_RENEW = "IDENTITY_RENEW"
        const val ACTION_IDENTITY_ENROLLED = "IDENTITY_ENROLLED"
        const val ACTION_IDENTITY_REVOKED = "IDENTITY_REVOKED"

        @JvmStatic
        lateinit var instance: SeaCat
            private set

        @JvmStatic
        fun init(context: Context, apiURL: String) {
            instance = SeaCat(context, apiURL)
        }

        val identity: Identity
            get() {  return instance.identity }

        val ready: Boolean
            get() { return instance.ready }

        val sslContext: SSLContext
            get() {  return instance.sslContext }


        internal val certificateFactory = CertificateFactory.getInstance("X.509")
        internal val executor = Executors.newSingleThreadScheduledExecutor()
    }

    val identity = Identity(this)
    val peers = PeerProvider(this)

    val ready: Boolean
        get() {
            if (identity.certificate == null) return false
            return identity.verify()
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
                                identity.certificate!!//,
//                                intermediateCA as X509Certificate
                            )
                        }

                        override fun getPrivateKey(alias: String?): PrivateKey {
                            assert(identityString.equals(alias))
                            return identity.privateKey!!
                        }

                    }

                ),
                controller.createTrustManagers(),
                null
            )

            return context
        }


    val trustManager: X509TrustManager
        get() {
            val trustManagers = controller.createTrustManagers()
            return trustManagers[0] as X509TrustManager
        }

    val executorService: ExecutorService
        get() {
            return executor
        }


    // Private parts

    internal fun constructApiURL(postfix: String): URL
    {
        val clean_url = apiURL.trim('/')
        if (postfix[0] != '/') {
            return URL(clean_url + '/' + postfix)
        } else {
            return URL(clean_url + postfix)
        }
    }

    internal val sharedPreferences: SharedPreferences
        get() {
            return context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        }

}
