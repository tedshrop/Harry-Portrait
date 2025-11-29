package com.example.fireseamlesslooper

import android.util.Log
import java.io.File

object UsbHelper {

    fun findUsbRoot(): String? {
        // First try USB mount in /mnt/media_rw/
        val usbMounts = File("/mnt/media_rw/").listFiles { file -> file.isDirectory } ?: emptyArray()
        for (mount in usbMounts) {
            val usbPath = mount.absolutePath + "/Movies"
            val usbDir = File(usbPath)
            if (usbDir.exists() && usbDir.canRead()) {
                Log.d("UsbHelper", "Found USB Movies directory: $usbPath")
                return usbPath
            }
        }

        // Fallback to internal storage (previous implementation)
        val fallbackPath = "/sdcard/Download/Output"
        Log.d("UsbHelper", "Using fallback internal storage path: $fallbackPath")
        return fallbackPath
    }
}
