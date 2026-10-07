package com.rustyranger.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object FileOpener {

    fun openFile(context: Context, filePath: String) {
        val file = File(cleanPath(filePath))
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist: ${file.name}", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val mimeType = getMimeType(file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "Open with..."))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getMimeType(file: File): String {
        val extension = file.extension.lowercase()
        if (extension.isEmpty()) return "*/*"

        return when (extension) {
            "apk" -> "application/vnd.android.package-archive"
            "pdf" -> "application/pdf"
            "txt", "log", "md", "rs", "kt", "java", "py", "js", "ts", "json", "xml", "html", "css" -> "text/plain"
            "jpg", "jpeg", "png", "webp", "gif", "bmp" -> "image/*"
            "mp4", "mkv", "webm", "avi", "mov" -> "video/*"
            "mp3", "flac", "ogg", "wav", "m4a" -> "audio/*"
            "zip", "tar", "gz", "7z", "rar" -> "application/zip"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
        }
    }

    fun cleanPath(uriOrPath: String): String {
        return if (uriOrPath.startsWith("file://")) {
            uriOrPath.substring(7)
        } else {
            uriOrPath
        }
    }
}
