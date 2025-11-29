package com.example.fireseamlesslooper.video

import android.util.Log
import java.io.File
import kotlin.random.Random

class WeightedVideoSelector(
    private val basePath: String,
    private val folderWeights: Map<String, Int> = mapOf(
        "common" to 860,
        "uncommon" to 133,
        "rare" to 15,
        "legendary" to 2
    )
) {

    private val videoExtensions = listOf(".mp4", ".mov", ".mkv", ".avi")

    fun nextVideo(): String? {
        val folder = pickWeightedFolder()
        val baseDir = File(basePath)
        Log.d("VideoSelector", "basePath: $basePath, selected folder: $folder")
        if (!baseDir.exists() || !baseDir.isDirectory) {
            Log.e("VideoSelector", "baseDir does not exist: $baseDir")
            return null
        }

        val folderDir = baseDir.listFiles()
            ?.firstOrNull { it.isDirectory && it.name.equals(folder, ignoreCase = true) }
            ?: File(baseDir, folder)

        Log.d("VideoSelector", "folderDir: $folderDir, exists: ${folderDir.exists()}, isDir: ${folderDir.isDirectory}")

        if (!folderDir.exists() || !folderDir.isDirectory) return null

        try {
            val filesList = folderDir.listFiles()
            if (filesList == null) {
                Log.e("VideoSelector", "listFiles() returned null for $folderDir")
                return null
            }
            val files = filesList.filter { f ->
                videoExtensions.any { ext -> f.name.lowercase().endsWith(ext) }
            }

            Log.d("VideoSelector", "files found: ${files.size}")
            if (files.isNotEmpty()) {
                Log.d("VideoSelector", "selected: ${files[0].absolutePath}")
            }

            return if (files.isEmpty()) null else files.random().absolutePath
        } catch (e: Exception) {
            Log.e("VideoSelector", "Exception reading files: ${e.message}")
            return null
        }
    }

    private fun pickWeightedFolder(): String {
        val total = folderWeights.values.sum()
        val r = Random.nextInt(total)
        var sum = 0
        for ((folder, weight) in folderWeights) {
            sum += weight
            if (r < sum) return folder
        }
        return "common"
    }
}
