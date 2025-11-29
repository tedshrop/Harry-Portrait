package com.example.fireseamlesslooper.usb

import android.util.Log
import java.io.File

object UsbFileRepository {

    private const val TAG = "USB_DIRECT"
    private val folders = listOf("Common", "Uncommon", "Rare", "Legendary")

    fun loadVideos(usbRoot: File): Map<String, List<File>> {
        val result = mutableMapOf<String, List<File>>()

        for (folder in folders) {
            val f = File(usbRoot, folder)
            val videos = if (f.exists() && f.isDirectory) {
                f.listFiles()?.filter { it.isFile && it.name.endsWith(".mp4") } ?: emptyList()
            } else {
                emptyList()
            }
            result[folder] = videos
            Log.d(TAG, "Loaded ${videos.size} videos from $folder folder")
        }

        val totalVideos = result.values.sumOf { it.size }
        Log.d(TAG, "Total videos loaded: $totalVideos from ${usbRoot.absolutePath}")

        return result
    }
}
