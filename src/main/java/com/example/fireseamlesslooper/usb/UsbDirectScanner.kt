package com.example.fireseamlesslooper.usb

import com.example.fireseamlesslooper.DebugPrinter
import java.io.File

data class UsbRoot(
    val uuid: String,
    val path: File
)

object UsbDirectScanner {
    private const val TAG = "USB_DIRECT"
    private val possibleRoots = listOf(
        File("/storage"),
        File("/mnt/media_rw"),
        File("/mnt/usbdrive"),
        File("/mnt"),
        File("/mnt/sdcard")
    )

    fun detectUsbRoot(): UsbRoot? {
        for (root in possibleRoots) {
            if (!root.exists()) {
DebugPrinter.log(TAG, "Mount point does not exist: ${root.absolutePath}")
                continue
            }

            root.listFiles()?.forEach { dir ->
                if (dir.isDirectory && dir.name.matches(Regex("[0-9A-F]{4}-[0-9A-F]{4}"))) {
                    if (dir.canRead()) {
DebugPrinter.log(TAG, "Found accessible USB drive: ${dir.absolutePath}")
                        return UsbRoot(dir.name, dir)
                    } else {
DebugPrinter.log(TAG, "USB drive exists but not readable: ${dir.absolutePath}")
                    }
                }
            }
        }
DebugPrinter.log(TAG, "No accessible USB drive found in any mount point")
        return null
    }
}
