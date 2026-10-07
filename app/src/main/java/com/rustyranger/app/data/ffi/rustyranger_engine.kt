package com.rustyranger.app.data.ffi

import android.os.Environment
import com.rustyranger.app.util.FileOpener
import java.io.File

data class FfiVfsNode(
    val uri: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val modifiedTimestamp: Long,
    val mimeType: String
)

data class FfiEngineEvent(
    val eventType: String,
    val payloadJson: String
)

data class FfiNavItem(
    val title: String,
    val uri: String,
    val iconName: String,
    val category: String
)

interface EventObserver {
    fun onEvent(event: FfiEngineEvent)
}

class PlatformEngineHandle private constructor() {

    fun listNodes(parentUri: String): List<FfiVfsNode> {
        val cleanPath = FileOpener.cleanPath(parentUri)
        val targetDir = File(cleanPath)

        if (!targetDir.exists() || !targetDir.isDirectory) {
            val root = Environment.getExternalStorageDirectory()
            return listFilesFromDisk(root)
        }

        return listFilesFromDisk(targetDir)
    }

    private fun listFilesFromDisk(directory: File): List<FfiVfsNode> {
        val path = directory.toPath()
        val result = mutableListOf<FfiVfsNode>()
        try {
            java.nio.file.Files.newDirectoryStream(path).use { stream ->
                for (childPath in stream) {
                    val attrs = java.nio.file.Files.readAttributes(childPath, java.nio.file.attribute.BasicFileAttributes::class.java)
                    val isDir = attrs.isDirectory
                    val name = childPath.fileName.toString()
                    val ext = name.substringAfterLast('.', "")
                    val mime = if (isDir) "inode/directory" else detectMimeString(ext)
                    result.add(
                        FfiVfsNode(
                            uri = "file://${childPath.toAbsolutePath()}",
                            name = name,
                            isDirectory = isDir,
                            sizeBytes = if (isDir) 0L else attrs.size(),
                            modifiedTimestamp = attrs.lastModifiedTime().toMillis() / 1000,
                            mimeType = mime
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Return whatever we have or empty list
        }

        result.sortWith(
            compareBy<FfiVfsNode> { !it.isDirectory }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        )
        return result
    }

    private fun detectMimeString(ext: String): String {
        return when (ext.lowercase()) {
            "rs", "kt", "java", "py", "js", "ts", "json", "xml" -> "text/code"
            "png", "jpg", "jpeg", "webp", "gif" -> "image/*"
            "mp4", "mkv", "webm" -> "video/*"
            "mp3", "flac", "ogg" -> "audio/*"
            "pdf" -> "application/pdf"
            "apk" -> "application/vnd.android.package-archive"
            "zip", "tar", "gz", "7z" -> "application/zip"
            else -> "application/octet-stream"
        }
    }

    fun startCopy(sourceUris: List<String>, targetDirUri: String): String {
        return "op_copy_${System.currentTimeMillis()}"
    }

    fun cancelOperation(opId: String) {}

    fun search(rootUri: String, query: String): List<FfiVfsNode> {
        val cleanRoot = FileOpener.cleanPath(rootUri)
        val rootDir = File(cleanRoot)
        if (!rootDir.exists()) return emptyList()

        val results = mutableListOf<FfiVfsNode>()
        rootDir.walkTopDown().maxDepth(4).forEach { file ->
            if (file.name.contains(query, ignoreCase = true)) {
                results.add(
                    FfiVfsNode(
                        uri = "file://${file.absolutePath}",
                        name = file.name,
                        isDirectory = file.isDirectory,
                        sizeBytes = if (file.isDirectory) 0L else file.length(),
                        modifiedTimestamp = file.lastModified() / 1000,
                        mimeType = if (file.isDirectory) "inode/directory" else detectMimeString(file.extension)
                    )
                )
            }
        }
        return results
    }

    fun getNavigationItems(): List<FfiNavItem> {
        val storageRoot = Environment.getExternalStorageDirectory().absolutePath
        return listOf(
            FfiNavItem("Internal Storage", "file://$storageRoot", "sd_storage", "Storage"),
            FfiNavItem("Downloads", "file://$storageRoot/Download", "download", "Storage"),
            FfiNavItem("Documents", "file://$storageRoot/Documents", "description", "Storage"),
            FfiNavItem("Pictures", "file://$storageRoot/Pictures", "image", "Media"),
            FfiNavItem("Movies", "file://$storageRoot/Movies", "movie", "Media"),
            FfiNavItem("Music", "file://$storageRoot/Music", "music_note", "Media")
        )
    }

    fun subscribeEvents(observer: EventObserver): String {
        return "sub_001"
    }

    fun unsubscribeEvents(subscriptionId: String) {}

    companion object {
        fun create(): PlatformEngineHandle {
            return PlatformEngineHandle()
        }
    }
}
