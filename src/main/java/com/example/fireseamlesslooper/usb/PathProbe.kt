package com.example.fireseamlesslooper.usb

import android.util.Log
import com.example.fireseamlesslooper.DebugPrinter
import java.io.File

object PathProbe {

    private const val TAG = "USB_MONITOR"
    private val expectedFolders = setOf("common", "uncommon", "rare", "legendary")

    /**
     * Probes standard FireOS/Android USB mount paths for USB storage containing video folders.
     * Returns a list of potential USB root directories.
     */
    fun probeUsbPaths(): List<String> {
        val candidatePaths = listOf(
            "/mnt/media_rw",
            "/mnt/media_rw/",
            "/storage",
            "/storage/usb",
            "/storage/",
            "/storage/self/primary/usb"
        )

        val usbRoots = mutableListOf<String>()

        for (basePath in candidatePaths) {
            try {
                DebugPrinter.log(TAG, "Probing path: $basePath")
                usbRoots.addAll(probePath(File(basePath)))
            } catch (e: Exception) {
                DebugPrinter.log(TAG, "Error probing $basePath: ${e.message}")
            }
        }

        DebugPrinter.log(TAG, "Found ${usbRoots.size} potential USB roots: $usbRoots")
        return usbRoots
    }

    private fun probePath(dir: File, maxDepth: Int = 2, currentDepth: Int = 0): List<String> {
        val roots = mutableListOf<String>()

        if (!dir.exists() || !dir.isDirectory || !dir.canRead()) {
            return roots
        }

        if (currentDepth <= maxDepth) {
            // Check if this directory is a USB root (contains expected folders)
            if (containsExpectedFolders(dir)) {
                DebugPrinter.log(TAG, "Found USB root at: ${dir.absolutePath}")
                roots.add(dir.absolutePath)
            }

            // Probe subdirectories
            val subDirs = dir.listFiles { file -> file.isDirectory } ?: emptyArray()
            for (subDir in subDirs) {
                roots.addAll(probePath(subDir, maxDepth, currentDepth + 1))
            }
        }

        return roots
    }

    private fun containsExpectedFolders(dir: File): Boolean {
        if (!dir.exists() || !dir.isDirectory) return false

        try {

            val videoExtensions = setOf("mp4", "mov", "mkv", "avi")
            var foundAnyVideo = false

            val files = dir.listFiles() ?: return false
            for (file in files) {
                if (file.isDirectory) {
                    val lowerName = file.name.lowercase()
                    if (expectedFolders.contains(lowerName)) {
                        // Check if this folder has video files
                        val videos = file.listFiles { f -> f.isFile && f.name.lowercase().matches(".*\\.(${videoExtensions.joinToString("|")})".toRegex()) }
                        if (videos?.isNotEmpty() == true) {
                            foundAnyVideo = true
                        }
                    }
                }
            }
            return foundAnyVideo

            } catch (e: Exception) {
            DebugPrinter.log(TAG, "Error checking folders in ${dir.absolutePath}: ${e.message}")
            return false
        }
    }
}
