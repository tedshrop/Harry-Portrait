package com.example.fireseamlesslooper.video

import android.net.Uri
import android.util.Log
import java.io.File
import com.example.fireseamlesslooper.usb.UsbFileRepository
import com.example.fireseamlesslooper.DebugPrinter

class VideoRepository(private val usbFileRepository: UsbFileRepository, private val usbRoot: File?) {

    private val TAG = "VIDEO_REPO"
    private var cachedVideos: Map<String, List<File>> = emptyMap()

    /**
     * Load videos for a given USB root
     */
    fun loadVideosForRoot(usbRoot: File) {
        cachedVideos = usbFileRepository.loadVideos(usbRoot)
    }

    /**
     * Clear cached videos
     */
    fun clearVideos() {
        cachedVideos = emptyMap()
    }

    /**
     * Get the next video URI using weighted selection from categorized videos
     */
    fun nextVideo(): Uri? {
        if (usbRoot == null || cachedVideos.isEmpty()) {
            DebugPrinter.log(TAG, "USB not available for video selection - usbRoot=$usbRoot cachedCount=${cachedVideos.values.sumOf{it.size}}")
            Log.w(TAG, "USB not available for video selection")
            return null
        }

        try {
            val categorizedVideos = cachedVideos
            if (categorizedVideos.isEmpty()) {
                DebugPrinter.log(TAG, "No categorized videos available - cachedCount=${cachedVideos.values.sumOf{it.size}}")
                Log.w(TAG, "No categorized videos available")
                return null
            }

            val selectedVideo = selectWeightedVideo(categorizedVideos)
            DebugPrinter.log(TAG, "selectWeightedVideo returned: ${selectedVideo?.absolutePath ?: "null"}")
            Log.d(TAG, "Selected video: ${selectedVideo?.name}")
            return if (selectedVideo != null) Uri.fromFile(selectedVideo) else null
        } catch (e: Exception) {
            DebugPrinter.log(TAG, "Exception in nextVideo: ${e.message}")
            Log.e(TAG, "Error selecting next video: ${e.message}", e)
            return null
        }
    }

    /**
     * Check if videos are available
     */
    fun hasVideos(): Boolean {
        return cachedVideos.values.sumOf { it.size } > 0
    }

    /**
     * Get total video count
     */
    fun getTotalVideoCount(): Int {
        return cachedVideos.values.sumOf { it.size }
    }

    /**
     * Select a video using weighted random selection based on categories
     */
    private fun selectWeightedVideo(categorizedVideos: Map<String, List<File>>): File? {
        if (categorizedVideos.isEmpty()) return null

        // Category weights (matching the original implementation)
        val folderWeights = mapOf(
            "common" to 60,
            "uncommon" to 25,
            "rare" to 10,
            "legendary" to 5
        )

        // Filter to only categories that have videos
        val availableCategories = categorizedVideos.filter { it.value.isNotEmpty() }

        if (availableCategories.isEmpty()) {
            DebugPrinter.log(TAG, "availableCategories empty after filter - cachedCount=${categorizedVideos.values.sumOf{it.size}} keys=${categorizedVideos.keys}")
            return null
        }

        // Calculate total weight (normalize category keys to lowercase to match folderWeights)
        val totalWeight = availableCategories.keys.sumOf { folderWeights[it.lowercase()] ?: 0 }
        if (totalWeight <= 0) return null

        // Pick weighted category
        DebugPrinter.log(TAG, "Available categories: ${availableCategories.keys} totalWeight=$totalWeight")
        val randomValue = kotlin.random.Random.nextInt(totalWeight)
        DebugPrinter.log(TAG, "randomValue=$randomValue")
        var cumulativeWeight = 0

        var selectedCategory: String? = null
        for ((category, videos) in availableCategories) {
            val weight = folderWeights[category.lowercase()] ?: 0
            cumulativeWeight += weight
            if (randomValue < cumulativeWeight) {
                selectedCategory = category
                break
            }
        }

        // Return random video from the selected category
        val categoryVideos = selectedCategory?.let { categorizedVideos[it] }
        val chosen = categoryVideos?.randomOrNull().also { selected ->
            if (selected != null) {
                Log.d(TAG, "Selected '${selected.name}' from category '$selectedCategory'")
                DebugPrinter.log(TAG, "Selected '${selected.name}' from category '$selectedCategory' path=${selected.absolutePath}")
            } else {
                DebugPrinter.log(TAG, "No video chosen from category '$selectedCategory' (videos list empty?)")
            }
        }
        return chosen
    }
}
