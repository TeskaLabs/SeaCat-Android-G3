package com.teskalabs.seacat

import android.util.Log
import com.teskalabs.seacat.miniasn1.MiniASN1
import com.teskalabs.seacat.misc.Base32
import com.teskalabs.seacat.misc.generateECKeyPair
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.*
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.Callable

//TODO: Ensure that verify() is called periodically b/c it starts identity certificate renew process if needed

// Identity is a basically combination of certificate + public key + private key
// It is used to represent a current application instance in a cryptography strong manner
class Identity(private val seacat: SeaCat) {

    private val TAG = "Identity"
    private val alias = "SeaCatIdentity"


    fun renew() {
        seacat.controller.onAction(SeaCat.ACTION_IDENTITY_RENEW)
        if (certificate == null) {
            seacat.controller.onInitialEnrollmentRequested(seacat)
        } else {
            seacat.controller.onReenrollmentRequested(seacat)
        }
    }


    // Load an identity
    // returns false, if identity failed to load
    internal fun load(): Boolean {
        if (certificate == null) {
            return false
        }

        if (!doesPrivateKeyExists()) {
            return false
        }

        return verify()
    }


    // Enroll my identity (initial enrollment of re-enrollment aka renewal)
    fun enroll(attributes: Map<String, String> = mapOf()) {
        // Get an existing identity keypair or generate a new one
        val keypair= keypair ?:
            generateECKeyPair(seacat.context, alias,false)
        enrollCertificateRequest(
            buildCertificateRequest(keypair, attributes)
        )
    }


    // Revoke the identity (
    fun revoke() {
        keyStore.deleteEntry(alias)
        keyStore.deleteEntry(alias + "Certificate")

        seacat.controller.onAction(SeaCat.ACTION_IDENTITY_REVOKED)

        //TODO: Send a revocation info to a SeaCat PKI
    }


    // Check validity of the identity certificate
    internal fun verify(): Boolean {
        val certificate = certificate ?: return false

        // Do a soft verification of the expiration, commence renewal if needed
        // If the identity certificate is after a half of its life
        // OR it is less than 30 days to expiration day
        // then start renew() process
        val nbf_millisec = certificate.notBefore.time
        val naf_millisec = certificate.notAfter.time
        val now_millisec = Date().time
        val half_millisec = (naf_millisec - nbf_millisec) / 2L
        val days30_millisec = 30 /* days */ * 24 /* hours in day */ * 60 /* minutes in hour */ * 60 /* seconds in minute */ * 1000L /* milliseconds in a second */
        if ((now_millisec > (naf_millisec - half_millisec)) or (now_millisec > (naf_millisec - days30_millisec))) {
            SeaCat.executor.submit(Callable {
                renew()
            })
        }

        if (nbf_millisec > now_millisec) return false; // Not valid yet
        if (naf_millisec < now_millisec) return false; // Not valid any longer

        //TODO: More verifications ...

        return true
    }


    // Build a certificate request by a freshly generated keypair
    private fun buildCertificateRequest(keypair: KeyPair, attributes: Map<String, String>): ByteArray {

        // Public key will added to a request in a DER format
        val public_key_encoded = keypair.public.getEncoded()

        // Get a creation and expiration date of the request
        val cal = Calendar.getInstance()
        val created_at = cal.time
        cal.add(Calendar.MINUTE, 5)  // CR is valid for 5 minutes
        val valid_to = cal.time

        //TODO: Validate that public_key_encoded is valid SubjectPublicKeyInfo for my key
        // That is basically done by checking if the beginning of the public_key_encoded is an ByteArray with a ASN.1 content (fixed, could be hardcoded)

        val transformed_attributes = mutableListOf( // Attributes
            MiniASN1.DER.SEQUENCE(arrayOf(
                MiniASN1.DER.IA5String("OS"),
                MiniASN1.DER.IA5String("android")
            ))
            //TODO: From BuildConfig, add BUILD_TYPE, FLAVOR, VERSION_CODE, VERSION_NAME
            // See https://stackoverflow.com/questions/23431354/how-to-get-the-build-variant-at-runtime-in-android-studio
        )

        for ((k, v) in attributes) {
            transformed_attributes.add(
                MiniASN1.DER.SEQUENCE(arrayOf(
                    MiniASN1.DER.IA5String(k),
                    MiniASN1.DER.IA5String(v)
                ))
            )
        }

        // Build a request body that will be signed (to-be-signed)
        var tbsRequest = MiniASN1.DER.SEQUENCE(arrayOf(
            MiniASN1.DER.INTEGER(0x1902),
            MiniASN1.DER.UTF8String(seacat.context.getPackageName()), // application
            MiniASN1.DER.UTCTime(created_at), // createdAt
            MiniASN1.DER.UTCTime(valid_to), // validTo
            MiniASN1.DER.SEQUENCE(arrayOf( // SubjectPublicKeyInfo
                MiniASN1.DER.SEQUENCE(arrayOf( // algorithm
                    MiniASN1.DER.OBJECT_IDENTIFIER("1.2.840.10045.2.1"), // ecPublicKey (ANSI X9.62 public key type)
                    MiniASN1.DER.OBJECT_IDENTIFIER("1.2.840.10045.3.1.7") // prime256v1 (ANSI X9.62 named elliptic curve)
                )),
                MiniASN1.DER.BIT_STRING(public_key_encoded.copyOfRange(public_key_encoded.size-65, public_key_encoded.size))
            )),
            MiniASN1.DER.SEQUENCE_OF(transformed_attributes.toTypedArray())
        ))
        tbsRequest = byteArrayOf(0xA0.toByte()) + tbsRequest.copyOfRange(1, tbsRequest.size)

        // Sign a request body
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(keypair.private)
        sig.update(tbsRequest)
        val signature = sig.sign()

        // Construct the signed and final certificate request
        return MiniASN1.DER.SEQUENCE(arrayOf(
            tbsRequest,
            MiniASN1.DER.SEQUENCE(arrayOf(
                MiniASN1.DER.OBJECT_IDENTIFIER("1.2.840.10045.4.3.2") // ecdsa-with-SHA256
            )),
            MiniASN1.DER.OCTET_STRING(signature)
        ))
    }


    // Submit certificat request to a server (CA) for an approval
    private fun enrollCertificateRequest(certificate_request: ByteArray) {

//        var x: String = ""
//        for (c in certificate_request) {
//            x = x + "%02X ".format(c)
//        }
//        Log.i(TAG, ">>> " + x)

        SeaCat.executor.submit(Callable {
            val url = URL(seacat.apiURL + "/enroll")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.doInput = true
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/octet-stream")

            try {
                val outputStream = DataOutputStream(connection.outputStream)
                outputStream.write(certificate_request)
                outputStream.flush()
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to upload the request body", exception)
            }

            if (connection.responseCode != 200) {
                //val output = connection.inputStream.readBytes()
                Log.e(TAG, "Failed to upload the request: %d".format(connection.responseCode))
                return@Callable
            }

            //TODO: Check Content-Type ... a certificate or JSON

            val certificate = SeaCat.certificateFactory.generateCertificate(connection.inputStream)
            keyStore.setCertificateEntry(alias + "Certificate", certificate)

            load()

            seacat.controller.onAction(SeaCat.ACTION_IDENTITY_ENROLLED)
        } )
    }


    ///

    // Get identity certificate
    val certificate: X509Certificate?
        get() {
            try {
                return keyStore.getCertificate(alias + "Certificate") as X509Certificate?
            }
            catch (e: KeyStoreException) {
                Log.w(TAG, "Failed to get certificate from a keystore", e)
                return null
            }
        }

    // Get identity public key
    val publicKey: PublicKey?
        get() { return certificate?.getPublicKey() }


    // Get a private key
    val privateKey: PrivateKey?
        get() {
            return keyStore.getKey(alias, null) as PrivateKey?
        }

    val keypair: KeyPair?
        get() {
            val private_key = privateKey ?: return null
            val public_key = publicKey ?: return null
            return KeyPair(public_key, private_key)
        }

    // Get Identity string
    override fun toString(): String
    {
        val cert = certificate ?: return ""
        return extractIdentity(cert) ?: ""
    }

    // Check if the identity private key is available
    private fun doesPrivateKeyExists(): Boolean {
        try {
            val key = keyStore.getKey(alias, null)
            if (key == null) return false
        } catch (e: GeneralSecurityException) {
            //Log.w(SeaCatInternals.L, "Failed to validate that the " + this.alias + " key exists:", e)
            return false
        }
        return true
    }

    // Get a loaded keystore used for a storage of the identity objects
    val keyStore: KeyStore
        get() {
            val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
            try {
                keyStore.load(null)
            } catch (e: IOException) {
                throw KeyStoreException("Key store error", e)
            }
            return keyStore
        }
}


private fun extractIdentity(publicKey: PublicKey): String? {
    val der = publicKey.encoded

    // For an elliptic curve public key, the format follows the ANSI X9.63 standard using a byte string of 04 || X || Y.
    val ecpubkey = der.copyOfRange(der.size - 65, der.size)
    assert(ecpubkey[0] == 4.toByte())

    val hash = MessageDigest.getInstance("SHA-384").digest(ecpubkey)
    val identity = Base32.encode(hash)

    return identity.substring(0, 16)
}

// Extract the identity string from a certificate
private fun extractIdentity(certificate: Certificate): String? {
    return extractIdentity(certificate.publicKey)
}
