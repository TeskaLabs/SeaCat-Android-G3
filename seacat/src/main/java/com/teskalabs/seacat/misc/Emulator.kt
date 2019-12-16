package com.teskalabs.seacat.misc

import android.os.Build

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
