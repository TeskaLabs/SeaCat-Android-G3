package com.teskalabs.seacat

import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.Certificate
import java.util.concurrent.Callable

class Peer(val seacat: SeaCat) {

    fun fetchCertificate(identity: String, completion: (certificate: Certificate) -> Unit) {

        //TODO: getPeerCertificateFromMemCache
        //TODO: loadPeerCertificateFromDirCache

        val future = SeaCat.executor.submit(PeerCertificateDownloadTask(seacat, identity))
        future.run {
            val certificate = this.get()
            if (certificate != null) {
                completion(certificate)
            }
        }
    }

    fun getCertificate(identity: String): Certificate {
        //TODO: Error handling (timeout, certificate not found)

        //TODO: getPeerCertificateFromMemCache
        //TODO: loadPeerCertificateFromDirCache

        val future = SeaCat.executor.submit(PeerCertificateDownloadTask(seacat, identity))
        val certificate = future.get()
        if (certificate != null) {
            return certificate
        } else {
            throw Exception("Failed to download a peer certificate for '$identity'")
        }
    }

}


private class PeerCertificateDownloadTask(val seacat: SeaCat, val identity: String) : Callable<Certificate?> {

    override fun call(): Certificate? {
        val url = URL(seacat.apiURL + "/get/$identity")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        //TODO: connection.responseCode != 200

        val certificate = SeaCat.certificateFactory.generateCertificate(connection.inputStream)

        //TODO: Validate

        return certificate
    }

}