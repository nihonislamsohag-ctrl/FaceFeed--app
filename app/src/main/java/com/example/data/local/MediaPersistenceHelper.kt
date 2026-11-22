package com.example.data.local

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

/**
 * Copies user-picked gallery photos, videos, and temporary audio files into permanent
 * internal app storage (context.filesDir/fotofilm_media/) so URIs stay valid forever
 * across app restarts, reboots, and expired content:// grants.
 */
object MediaPersistenceHelper {

    fun persistUriIfLocal(
        context: Context,
        rawUri: String,
        subDir: String = "posts",
        defaultExtension: String = "dat"
    ): String {
        val trimmed = rawUri.trim()
        if (trimmed.isBlank()) return trimmed
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return trimmed
        }

        val appCtx = context.applicationContext
        val permanentDir = File(appCtx.filesDir, "fotofilm_media/$subDir")
        if (!permanentDir.exists()) {
            permanentDir.mkdirs()
        }

        // If it is already inside permanent filesDir (fotofilm_media or voice_notes), keep it!
        val voiceNotesDir = File(appCtx.filesDir, "voice_notes").absolutePath
        val existingPath = when {
            trimmed.startsWith("/") -> trimmed
            trimmed.startsWith("file://") -> Uri.parse(trimmed).path
            else -> null
        }
        if (existingPath != null &&
            (existingPath.startsWith(permanentDir.absolutePath) || existingPath.startsWith(voiceNotesDir)) &&
            File(existingPath).exists() &&
            File(existingPath).length() > 0L
        ) {
            return Uri.fromFile(File(existingPath)).toString()
        }

        return try {
            val parsed = Uri.parse(trimmed)
            val ext = resolveExtension(appCtx, parsed, trimmed, defaultExtension)
            val destFile = File(
                permanentDir,
                "ff_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$ext"
            )

            val copied = if (existingPath != null) {
                val srcFile = File(existingPath)
                if (srcFile.exists() && srcFile.length() > 0L) {
                    FileInputStream(srcFile).use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    true
                } else {
                    false
                }
            } else {
                appCtx.contentResolver.openInputStream(parsed)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                    true
                } ?: false
            }

            if (copied && destFile.exists() && destFile.length() > 0L) {
                Uri.fromFile(destFile).toString()
            } else {
                try {
                    if (destFile.exists()) destFile.delete()
                } catch (_: Throwable) {
                }
                trimmed
            }
        } catch (_: Throwable) {
            trimmed
        }
    }

    private fun resolveExtension(
        context: Context,
        uri: Uri,
        raw: String,
        fallback: String
    ): String {
        try {
            val mime = context.contentResolver.getType(uri)
            if (!mime.isNullOrBlank()) {
                val mapped = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
                if (!mapped.isNullOrBlank()) return mapped
                when {
                    mime.startsWith("image/") -> return "jpg"
                    mime.startsWith("video/") -> return "mp4"
                    mime.startsWith("audio/") -> return "m4a"
                }
            }
        } catch (_: Throwable) {
        }
        val path = uri.path ?: raw
        val dotIdx = path.lastIndexOf('.')
        if (dotIdx in 1 until path.length - 1) {
            val ext = path.substring(dotIdx + 1).lowercase()
            if (ext.length in 2..5 && ext.all { it.isLetterOrDigit() }) {
                return ext
            }
        }
        return fallback
    }
}
