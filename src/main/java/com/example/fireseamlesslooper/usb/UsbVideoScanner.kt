package com.example.fireseamlesslooper.usb

import android.util.Log
import androidx.documentfile.provider.DocumentFile

class UsbVideoScanner {

    companion object {
        private const val TAG = "USB_SAF_DEBUG"
        private val SUPPORTED_EXTENSIONS = setOf("mp4", "mov", "mkv", "avi", "m4v", "3gp", "webm")
    }

    /**
     * Scan the USB root DocumentFile for video categories and return organized video files
     * Expected folder structure:
     * /Common/
     * /Uncommon/
     * /Rare/
     * /Legendary/
     *
     * Returns a map of category name to list of video DocumentFiles
     */
    fun scanVideos(root: DocumentFile): Map<String, List<DocumentFile>> {
        val categorizedVideos = mutableMapOf<String, MutableList<DocumentFile>>()

        try {
            Log.d(TAG, "Scanning USB root: ${root.uri}")

            // Look for category folders
            root.listFiles().forEach { file ->
                if (file.isDirectory) {
                    val categoryName = file.name?.lowercase()
                    Log.d(TAG, "Found directory: $categoryName")

                    when (categoryName) {
                        "common", "uncommon", "rare", "legendary" -> {
                            val videoFiles = scanCategoryFolder(file)
                            if (videoFiles.isNotEmpty()) {
                                categorizedVideos[categoryName] = videoFiles.toMutableList()
                                Log.d(TAG, "Found ${videoFiles.size} videos in $categoryName")
                            }
                        }
                    }
                }
            }

            // Log summary
            categorizedVideos.forEach { (category, videos) ->
                Log.d(TAG, "Category $category: ${videos.size} videos")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error scanning USB videos: ${e.message}", e)
            // Return empty result on error
        }

        return categorizedVideos
    }

    /**
     * Scan a specific category folder for video files
     */
    private fun scanCategoryFolder(categoryDir: DocumentFile): List<DocumentFile> {
        return try {
            categoryDir.listFiles()
                .filter { file ->
                    if (!file.isFile) return@filter false

                    val fileName = file.name?.lowercase() ?: ""
                    val isVideoFile = SUPPORTED_EXTENSIONS.any { ext ->
                        fileName.endsWith(".$ext")
                    }

                    if (isVideoFile) {
                        Log.d(TAG, "Found video file: ${file.name} (size: ${file.length()})")
                    }

                    isVideoFile
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning category folder ${categoryDir.name}: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get total count of videos across all categories
     */
    fun getTotalVideoCount(videosByCategory: Map<String, List<DocumentFile>>): Int {
        return videosByCategory.values.sumOf { it.size }
    }

    /**
     * Get all videos flattened into a single list (for selection without categories)
     */
    fun getAllVideos(videosByCategory: Map<String, List<DocumentFile>>): List<DocumentFile> {
        return videosByCategory.values.flatten()
    }
}
