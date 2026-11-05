package com.mohrix.ff

import android.util.Log

object NativeLib {
    init {
        try {
            System.loadLibrary("mohrix")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("MohrixFF", "Native library not found. Stub mode.")
        }
    }

    fun toggleFeature(feature: String, enable: Boolean) {
        Log.d("MohrixFF", "Feature: $feature -> $enable")
    }
}
