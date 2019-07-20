package com.teskalabs.seacat

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.util.*


// From https://stackoverflow.com/questions/2799097/how-can-i-detect-when-an-android-application-is-running-in-the-emulator
fun isEmulator(): Boolean {
    return (
           Build.FINGERPRINT.startsWith("generic")
        || Build.FINGERPRINT.startsWith("unknown")
        || Build.MODEL.contains("google_sdk")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK built for x86")
        || Build.MANUFACTURER.contains("Genymotion")
        || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
        || "google_sdk" == Build.PRODUCT
    )
}


// Produce a device/user unique identifier (non-cryptographic strength)
fun getUniqueIdentifier(context: Context): String {
    var deviceUuid: UUID? = null

    // This part is actually optional and could be commented out if e.g. Google restricts usage of ANDROID_ID
    val androidId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)
    if ((androidId != null) && (androidId != "9774d56d682e549c")) {
        deviceUuid = UUID.nameUUIDFromBytes(androidId.toByteArray(charset("utf8")))
    }

    if (deviceUuid == null) {
        val prefs = context.getSharedPreferences("com.teskalabs.seacat", Context.MODE_PRIVATE)

        val stored_uuid = prefs.getString("SeaCat::uniqueIdentifier",null);
        if (stored_uuid != null) {
            return stored_uuid
        }

        Log.w("SeaCat", "Settings.Secure.ANDROID_ID get failed, using random UUID")
        deviceUuid = UUID.randomUUID()

        with (prefs.edit()) {
            putString("SeaCat::uniqueIdentifier", deviceUuid.toString())
            apply()
        }
    }

    return deviceUuid.toString()
}
