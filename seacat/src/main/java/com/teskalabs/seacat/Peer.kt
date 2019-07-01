package com.teskalabs.seacat

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.Certificate
import java.util.concurrent.Callable

class Peer(private val seacat: SeaCat) {

    fun fetchCertificate(identity: String, completion: (certificate: Certificate?) -> Unit) {

        //TODO: getPeerCertificateFromMemCache
        //TODO: loadPeerCertificateFromDirCache

        SeaCat.executor.submit {
            val t = PeerCertificateDownloadTask(seacat, identity)
            val certificate= t.call()
            completion(certificate)
    }
    }


    fun getCertificate(identity: String): Certificate {
        //TODO: Error handling (timeout, certificate not found)

        //TODO: getPeerCertificateFromMemCache
        //TODO: loadPeerCertificateFromDirCache

        val task = PeerCertificateDownloadTask(seacat, identity)
        val certificate = task.call()
        if (certificate != null) {
            return certificate
        } else {
            throw Exception("Failed to download a peer certificate for '$identity'")
        }
    }

}


private class PeerCertificateDownloadTask(private val seacat: SeaCat, private val identity: String) : Callable<Certificate?> {

    override fun call(): Certificate? {
        val url = URL(seacat.apiURL + "/get/$identity")

        val certificate: Certificate = with (url.openConnection() as HttpURLConnection ) {
            requestMethod = "GET"
            connectTimeout = 3000

            if (responseCode != 200) {
                Log.w(SeaCat.TAG, "Invalid response code from $url: " + responseCode)
                return null
            }

            try {
                SeaCat.certificateFactory.generateCertificate(inputStream)
            } catch (exception: java.lang.Exception) {
                Log.w(SeaCat.TAG, "Failed to download a certificate", exception)
                return null
            }
        }

        //TODO: Validate
        return certificate
    }

}