package com.teskalabs.seacat

import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.Certificate
import java.util.concurrent.Callable

class PeerProvider(private val seacat: SeaCat) {

    val AuthorizedPeersDir = File(seacat.context.getFilesDir(), "authorized_peers")
    val DiskCacheDir = File(seacat.context.getFilesDir(), "peer_certs")
    private val MemCache: MutableMap<String, Certificate> = HashMap<String, Certificate>()

    init {
        AuthorizedPeersDir.mkdirs()
        DiskCacheDir.mkdirs()
    }

    //

    fun fetchCertificate(identity: String, completion: (certificate: Certificate?) -> Unit) {

        val cachedCertificate: Certificate? = getCertificateFromCaches(identity)
        if (cachedCertificate != null) {
            completion(cachedCertificate)
            return
        }

        SeaCat.executor.submit {
            val t = PeerCertificateDownloadTask(seacat, identity, DiskCacheDir)
            val certificate= t.call()
            if (certificate != null) { MemCache[identity] = certificate }
            completion(certificate)
        }
    }


    fun getCertificate(identity: String): Certificate {
        //TODO: Error handling (timeout, certificate not found)

        val cachedCertificate: Certificate? = getCertificateFromCaches(identity)
        if (cachedCertificate != null) return cachedCertificate

        val task = PeerCertificateDownloadTask(seacat, identity, DiskCacheDir)
        val certificate = task.call()
        if (certificate != null) {
            MemCache[identity] = certificate
            return certificate
        } else {
            throw Exception("Failed to download a peers certificate for '$identity'")
        }
    }

    private fun getCertificateFromCaches(identity: String): Certificate? {
        val memCachedCertificate = MemCache.get(identity)
        if (memCachedCertificate != null) return memCachedCertificate

        try {
            val diskCachedCertificate = with(File(DiskCacheDir, "$identity.der")) {
                with(this.inputStream()) {
                    SeaCat.certificateFactory.generateCertificate(this)
                }
            }
            MemCache[identity] = diskCachedCertificate
            return diskCachedCertificate
        }
        catch (exception: FileNotFoundException) {
            return null
        }

    }

    ///

    fun authorize(identity: String, roles: List<String>) {

        if (roles.size == 0) {
            File(AuthorizedPeersDir, "$identity.auth").delete()
            return
        }

        val body = identity + "\n" + roles.joinToString(", ") + "\n"
        //TODO: A digital signature
        with (File(AuthorizedPeersDir, "$identity.auth") ) {
            writeText(body, Charsets.UTF_8)
        }

    }

    fun getAuthorized(predicate: (roles: List<String>) -> Boolean): List<String> {
        // Scan the authorized peers directory
        val ret: MutableList<String> = mutableListOf()
        for (file in AuthorizedPeersDir.listFiles { file -> file?.isFile ?: false }) {
            val content = file.readText()
            val identity = content.lines()[0]
            val roles = content.lines()[1].split(", ")
            if (predicate(roles)) ret.add(identity)
        }
        return ret
    }

}


private class PeerCertificateDownloadTask(private val seacat: SeaCat, private val identity: String, private val diskcachedir: File) : Callable<Certificate?> {

    override fun call(): Certificate? {
        val url = URL(seacat.apiURL + "/get/$identity")

        val certificate: Certificate = with (url.openConnection() as HttpURLConnection ) {
            requestMethod = "GET"
            connectTimeout = 3000
            readTimeout = 3000

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

        // Write to a disk cache
        with (File(diskcachedir, "$identity.der") ) {
            writeBytes(certificate.encoded)
        }

        return certificate
    }

}
