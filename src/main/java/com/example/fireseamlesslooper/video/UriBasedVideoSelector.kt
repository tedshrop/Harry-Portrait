package com.example.fireseamlesslooper.video

import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import kotlin.random.Random

class UriBasedVideoSelector(
    baseDocument: DocumentFile,
    private val folderWeights: Map<String, Int> = mapOf(
        "common" to 80,
        "uncommon" to 16,
        "rare" to 3,
        "legendary" to 1
    )
) {

    private val TAG = "URI_VIDEO_SELECTOR"
    private val videoExtensions = setOf("mp4", "mov", "mkv", "avi")

    // Cache folder document files for performance
    private val folderDocuments: Map<String, DocumentFile>

    init {
        folderDocuments = buildFolderMap(baseDocument)
        Log.d(TAG, "Initialized with folders: ${folderDocuments.keys}")
    }

    private fun buildFolderMap(baseDocument: DocumentFile): Map<String, DocumentFile> {
        val folders = mutableMapOf<String, DocumentFile>()

        try {
            baseDocument.listFiles().forEach { file ->
                if (file.isDirectory) {
                    val folderName = file.name?.lowercase()
                    if (folderName != null && folderWeights.containsKey(folderName)) {
                        folders[folderName] = file
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error building folder map: ${e.message}")
        }

        return folders
    }

    fun nextVideo(): Uri? {
        val folderName = pickWeightedFolder()
        val folderDoc = folderDocuments[folderName]

        if (folderDoc == null) {
            Log.w(TAG, "No document found for folder: $folderName")
            return null
        }

        Log.d(TAG, "Selecting video from folder: $folderName (uri: ${folderDoc.uri})")

        try {
            val videoFiles = getVideoFiles(folderDoc)
            if (videoFiles.isEmpty()) {
                Log.w(TAG, "No video files found in folder: $folderName")
                return null
            }

            val selectedFile = videoFiles.random()
            Log.d(TAG, "Selected video: ${selectedFile.name} (${selectedFile.uri})")
            return selectedFile.uri

        } catch (e: Exception) {
            Log.e(TAG, "Error selecting video from folder $folderName: ${e.message}")
            return null
        }
    }

    private fun getVideoFiles(folderDoc: DocumentFile): List<DocumentFile> {
        return try {
            folderDoc.listFiles().filter { file ->
                if (!file.isFile) return@filter false

                val name = file.name?.lowercase() ?: ""
                val hasVideoExtension = videoExtensions.any { ext -> name.endsWith(ext) }

                if (hasVideoExtension) {
                    Log.d(TAG, "Found video file: ${file.name} (size: ${file.length()})")
                }

                hasVideoExtension
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing files in folder: ${e.message}")
            emptyList()
        }
    }

    private fun pickWeightedFolder(): String {
        val availableFolders = folderDocuments.keys.filter { folderName ->
            val folderDoc = folderDocuments[folderName]
            folderDoc != null && try {
                getVideoFiles(folderDoc).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }

        if (availableFolders.isEmpty()) {
            Log.w(TAG, "No folders with videos available, defaulting to 'common'")
            return "common"
        }

        // Recalculate weights for available folders only
        val totalWeight = availableFolders.sumOf { folderWeights[it] ?: 0 }
        if (totalWeight <= 0) return availableFolders.first()

        val r = Random.nextInt(totalWeight)
        var sum = 0

        for (folder in availableFolders) {
            val weight = folderWeights[folder] ?: 0
            sum += weight
            if (r < sum) {
                Log.d(TAG, "Picked folder: $folder (weight: $weight, total: $totalWeight)")
                return folder
            }
        }

        return availableFolders.first()
    }

    fun hasVideos(): Boolean {
        return folderDocuments.values.any { folderDoc ->
            try {
                getVideoFiles(folderDoc).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
    }

    fun getVideoCount(): Int {
        return folderDocuments.values.sumOf { folderDoc ->
            try {
                getVideoFiles(folderDoc).size
            } catch (e: Exception) {
                0
            }
        }
    }
}
