package com.teskalabs.seacat.misc

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator


// Generate a private and public key for an identity
fun generateECKeyPair(context: Context, alias: String, requireUserAuth: Boolean): KeyPair {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return generate_api19_22(context, alias, requireUserAuth)
    } else {
        return generate_api23(alias, requireUserAuth)
    }
}


@TargetApi(Build.VERSION_CODES.KITKAT)
private fun generate_api19_22(context: Context, alias: String, requireUserAuth: Boolean): KeyPair {
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
private fun generate_api23(alias: String, requireUserAuth: Boolean): KeyPair {

    val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
        alias,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    ).run {
        setDigests(
            KeyProperties.DIGEST_NONE, // TLS/SSL stacks generateECKeyPair the digest(s) themselves
            KeyProperties.DIGEST_SHA256,
            KeyProperties.DIGEST_SHA512
        )
        if (requireUserAuth) {
            setUserAuthenticationRequired(true)
            setUserAuthenticationValidityDurationSeconds(5)
        }
        //TODO: setIsStrongBoxBacked(true) ... consider this
        build()
    }

    val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
    kpg.initialize(parameterSpec)

    val kp = kpg.generateKeyPair()

    return kp
}
