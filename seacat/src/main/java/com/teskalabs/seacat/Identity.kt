package com.teskalabs.seacat

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.teskalabs.seacat.miniasn1.MiniASN1
import com.teskalabs.seacat.misc.Base32
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.*
import java.security.cert.Certificate
import java.util.*
import java.util.concurrent.Callable

//TODO: Renewal of the identity certificate
//TODO: Revocation of the identity certificate


// Identity is a basically combination of certificate + public key + private key
// It is used to represent a current application instance in a cryptography strong manner
class Identity(private val seacat: SeaCat) {

    private val TAG = "Identity"
    private val alias = "SeaCatIdentity"

    init {
        if (!load()) {
            seacat.controller.enroll(seacat)
        } else {
            check()
        }
    }


    // Load an identity
    // returns false, if identity failed to load
    private fun load(): Boolean {
        if (certificate == null) {
            return false
        }

        if (!privateKeyExists()) {
            return false
        }

        check()

        return true
    }


    // Generate a new identity
    fun enroll(attributes: Map<String, String> = mapOf()) {
        reset()
        val keypair = generate(seacat.context, false)
        val cr = buildCertificateRequest(keypair, attributes)
        enrollCertificateRequest(cr)
    }


    // Remove the identity (
    fun reset() {
        keyStore.deleteEntry(alias)
        keyStore.deleteEntry(alias + "Certificate")

        // There is a new identity now, broadcast it
        val intent = Intent()
        intent.addCategory(SeaCat.CATEGORY_SEACAT)
        intent.action = SeaCat.ACTION_IDENTITY_RESET
        seacat.broadcastManager.sendBroadcast(intent)

    }


    // Check validity of the identity certificate
    private fun check() {
        //TODO: This ...
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
            MiniASN1.DER.IA5String("OS"), MiniASN1.DER.OCTET_STRING("android".toByteArray())
            //TODO: From BuildConfig, add BUILD_TYPE, FLAVOR, VERSION_CODE, VERSION_NAME
            // See https://stackoverflow.com/questions/23431354/how-to-get-the-build-variant-at-runtime-in-android-studio
        )

        for ((k, v) in attributes) {
            transformed_attributes.add(MiniASN1.DER.IA5String(k))
            transformed_attributes.add(MiniASN1.DER.OCTET_STRING(v.toByteArray()))
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
            //MiniASN1.DER.SEQUENCE_OF(transformed_attributes.toTypedArray())
            MiniASN1.DER.SEQUENCE_OF(arrayOf())
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

            // There is a new identity now, broadcast it
            val intent = Intent()
            intent.addCategory(SeaCat.CATEGORY_SEACAT)
            intent.action = SeaCat.ACTION_IDENTITY_ESTABLISHED
            seacat.broadcastManager.sendBroadcast(intent)

        } )
    }


    ///

    // Get identity certificate
    val certificate: Certificate?
        get() {
            try {
                return keyStore.getCertificate(alias + "Certificate")
            }
            catch (e: KeyStoreException) {
                Log.w(TAG, "Failed to get certificate from a keystore", e)
                return null
            }
        }

    // Get identity public key
    val publicKey: PublicKey?
        get() { return certificate?.getPublicKey() }

    // Get Identity string
    override fun toString(): String
    {
        val cert = certificate ?: return ""
        return extractIdentity(cert) ?: ""
    }

    // Get a private key
    val privateKey: PrivateKey?
        get() {
            return keyStore.getKey(alias, null) as PrivateKey
        }

    // Check if the identity private key is available
    private fun privateKeyExists(): Boolean {
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


    ///

    // Generate a private and public key for an identity
    fun generate(context: Context, requireUserAuth: Boolean): KeyPair {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return generate_api19_22(context, requireUserAuth)
        } else {
            return generate_api23(requireUserAuth)
        }


    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun generate_api19_22(context: Context, requireUserAuth: Boolean): KeyPair {
        // https://stackoverflow.com/questions/50130448/generate-elliptic-curve-keypair-via-keystore-on-api-level-23

        val applicationContext = context.getApplicationContext()

        val builder = KeyPairGeneratorSpec.Builder(applicationContext)
        builder.setAlias(alias)
        builder.setKeySize(256)
        builder.setKeyType("EC")
//        builder.setStartDate(valid_from)
//        builder.setEndDate(valid_to)
//        builder.setSerialNumber(BigInteger.valueOf(serialNumber))
//        builder.setSubject(subjectPrincipal)

        if (requireUserAuth) {
            builder.setEncryptionRequired()
        }

        val kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
        kpg.initialize(builder.build())

        val kp = kpg.generateKeyPair()
        return kp
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun generate_api23(requireUserAuth: Boolean): KeyPair {

        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).run {
            setDigests(
                KeyProperties.DIGEST_NONE, // TLS/SSL stacks generate the digest(s) themselves
                KeyProperties.DIGEST_SHA256,
                KeyProperties.DIGEST_SHA512
            )
            if (requireUserAuth) {
                setUserAuthenticationRequired(true)
                setUserAuthenticationValidityDurationSeconds(5)
            }
            build()
        }

        val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
        kpg.initialize(parameterSpec)

        val kp = kpg.generateKeyPair()

        return kp
    }

}


fun extractIdentity(publicKey: PublicKey): String? {
    val der = publicKey.encoded

    // For an elliptic curve public key, the format follows the ANSI X9.63 standard using a byte string of 04 || X || Y.
    val ecpubkey = der.copyOfRange(der.size - 65, der.size)
    assert(ecpubkey[0] == 4.toByte())

    val hash = MessageDigest.getInstance("SHA-384").digest(ecpubkey)
    val identity = Base32.encode(hash)

    return identity.substring(0, 16)
}

// Extract the identity string from a certificate
fun extractIdentity(certificate: Certificate): String? {
    return extractIdentity(certificate.publicKey)
}
