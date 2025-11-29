package com.example.fireseamlesslooper.usb

import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import com.example.fireseamlesslooper.DebugPrinter

data class UsbVolume(val absolutePath: String, val isRemovable: Boolean)

object UsbVolumeResolver {

    private const val TAG = "USB_MONITOR"

    fun resolveUsbVolume(context: Context): UsbVolume? {
        DebugPrinter.log(TAG, "Resolving USB volume...")

        val storageManager = tryResolveViaStorageManager(context)
        if (storageManager != null) {
            DebugPrinter.log(TAG, "Resolved via StorageManager: ${storageManager.absolutePath}")
            return storageManager
        }

        val probed = PathProbe.probeUsbPaths()
        if (probed.isNotEmpty()) {
            val path = probed.first()
            DebugPrinter.log(TAG, "Resolved via probing: $path")
            return UsbVolume(path, true)
        }

        DebugPrinter.log(TAG, "No USB volume resolved")
        return null
    }

    private fun tryResolveViaStorageManager(context: Context): UsbVolume? {
        return try {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val volumes = storageManager.storageVolumes

            for (volume in volumes) {
                if (volume.isRemovable && volume.state == Environment.MEDIA_MOUNTED) {
                    val path = getVolumePath(volume)
                    if (path != null) {
                        DebugPrinter.log(TAG, "Found removable volume mounted: $path")
                        return UsbVolume(path, true)
                    }
                }
            }
            null
        } catch (e: Exception) {
            DebugPrinter.log(TAG, "StorageManager resolution failed: ${e.message}")
            null
        }
    }

    private fun getVolumePath(volume: StorageVolume): String? {
        return try {
            val method = StorageVolume::class.java.getMethod("getPath")
            method.invoke(volume) as String?
        } catch (e: Exception) {
            DebugPrinter.log(TAG, "Failed to get path from volume $volume: ${e.message}")
            null
        }
    }
}
