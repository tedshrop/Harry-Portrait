package com.example.fireseamlesslooper

import android.util.Log
import java.io.File
import kotlin.random.Random

class VideoSelector(private val basePath: String) {

    private val weights = mapOf(
        "common" to 70,
        "uncommon" to 20,
        "rare" to 9,
        "legendary" to 1
    )

    private val videoExtensions = listOf(".mp4", ".mov", ".mkv", ".avi")

    fun nextVideo(): String? {
        val folder = pickWeightedFolder()
        val baseDir = File(basePath)
        Log.d("VideoSelector", "basePath: $basePath")
        if (!baseDir.exists() || !baseDir.isDirectory) {
            Log.d("VideoSelector", "baseDir does not exist: $baseDir")
            return null
        }

        // Find a matching folder case-insensitively (e.g., "Common" or "common")
        val folderDir = baseDir.listFiles()
            ?.firstOrNull { it.isDirectory && it.name.equals(folder, ignoreCase = true) }
            ?: File(baseDir, folder)

        Log.d("VideoSelector", "folder: $folder, folderDir: $folderDir, exists: ${folderDir.exists()}, isDirectory: ${folderDir.isDirectory}")

        if (!folderDir.exists() || !folderDir.isDirectory) return null

        try {
            val filesList = folderDir.listFiles()
            if (filesList == null) {
                Log.e("VideoSelector", "listFiles() returned null, likely no read permission on $folderDir")
                return null
            }
            val files = filesList.filter { f -> videoExtensions.any { ext -> f.name.lowercase().endsWith(ext) } }

            Log.d("VideoSelector", "files count: ${files.size}")
            if (files.isNotEmpty()) {
                Log.d("VideoSelector", "sample file: ${files[0].absolutePath}")
            }

            return if (files.isEmpty()) null else files.random().absolutePath
        } catch (e: Exception) {
            Log.e("VideoSelector", "Exception reading files: ${e.message}")
            return null
        }
    }

    private fun pickWeightedFolder(): String {
        val total = weights.values.sum()
        val r = Random.nextInt(total)
        var sum = 0
        for ((folder, weight) in weights) {
            sum += weight
            if (r < sum) return folder
        }
        return "common"
    }
}
