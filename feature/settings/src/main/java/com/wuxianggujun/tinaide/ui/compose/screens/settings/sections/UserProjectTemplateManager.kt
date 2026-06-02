package com.wuxianggujun.tinaide.ui.compose.screens.settings.sections

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.wuxianggujun.tinaide.project.ProjectTemplateMetadata
import com.wuxianggujun.tinaide.project.ProjectTemplateMetadataReader
import com.wuxianggujun.tinaide.storage.ProjectPaths
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import java.util.zip.ZipFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class UserProjectTemplateItem(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val metadata: ProjectTemplateMetadata? = null,
)

internal enum class UserProjectTemplateFailure {
    NOT_ZIP,
    INVALID_NAME,
    CANNOT_READ,
    INVALID_ZIP,
    IMPORT_FAILED,
    RENAME_FAILED,
    EXPORT_FAILED,
    DELETE_FAILED,
    UNSAFE_PATH,
}

internal class UserProjectTemplateException(
    val failure: UserProjectTemplateFailure,
    cause: Throwable? = null,
) : Exception(cause?.message, cause)

internal object UserProjectTemplateManager {
    private val invalidFileNameChars = Regex("[\\\\/:*?\"<>|\\p{Cntrl}]+")
    private val whitespace = Regex("\\s+")

    fun listTemplates(templatesDir: File): List<UserProjectTemplateItem> {
        return templatesDir
            .takeIf { it.isDirectory }
            ?.listFiles { file -> file.isFile && isZipFileName(file.name) }
            ?.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            ?.map(::toItem)
            .orEmpty()
    }

    suspend fun importTemplateFromUri(
        context: Context,
        uri: Uri,
    ): Result<UserProjectTemplateItem> = withContext(Dispatchers.IO) {
        runCatching {
            val sourceName = queryDisplayName(context, uri)
                ?: uri.lastPathSegment?.substringAfterLast('/')
            val input = context.contentResolver.openInputStream(uri)
                ?: throw UserProjectTemplateException(UserProjectTemplateFailure.CANNOT_READ)
            input.use {
                importTemplate(
                    templatesDir = ProjectPaths.getUserProjectTemplatesRoot(context),
                    sourceName = sourceName,
                    input = it,
                )
            }
        }
    }

    fun importTemplate(
        templatesDir: File,
        sourceName: String?,
        input: InputStream,
    ): UserProjectTemplateItem {
        val safeName = sanitizeTemplateFileName(sourceName)
        if (!isZipFileName(sourceName) || !isZipFileName(safeName)) {
            throw UserProjectTemplateException(UserProjectTemplateFailure.NOT_ZIP)
        }

        val root = ProjectPaths.ensureDir(templatesDir).canonicalFile
        val target = resolveUniqueTarget(root, safeName)
        val tempFile = File(root, ".${target.name}.${System.nanoTime()}.tmp")

        return try {
            tempFile.outputStream().use { output -> input.copyTo(output) }
            runCatching { ZipFile(tempFile).use { } }
                .onFailure { throw UserProjectTemplateException(UserProjectTemplateFailure.INVALID_ZIP, it) }

            if (!tempFile.renameTo(target)) {
                throw UserProjectTemplateException(UserProjectTemplateFailure.IMPORT_FAILED)
            }
            toItem(target)
        } catch (error: UserProjectTemplateException) {
            tempFile.delete()
            throw error
        } catch (error: Throwable) {
            tempFile.delete()
            throw UserProjectTemplateException(UserProjectTemplateFailure.IMPORT_FAILED, error)
        }
    }

    fun renameTemplate(
        templatesDir: File,
        currentName: String,
        desiredName: String,
    ): UserProjectTemplateItem {
        if (desiredName.isBlank()) {
            throw UserProjectTemplateException(UserProjectTemplateFailure.INVALID_NAME)
        }

        return try {
            val root = ProjectPaths.ensureDir(templatesDir).canonicalFile
            val source = resolveExistingTemplate(root, currentName)
            val safeName = sanitizeTemplateFileName(desiredName)
            if (!isZipFileName(safeName)) {
                throw UserProjectTemplateException(UserProjectTemplateFailure.INVALID_NAME)
            }

            val target = File(root, safeName).canonicalFile
            if (!target.isInside(root) || target == root || target.isDirectory) {
                throw UserProjectTemplateException(UserProjectTemplateFailure.UNSAFE_PATH)
            }
            if (target == source) {
                return toItem(source)
            }
            if (target.exists() || !source.renameTo(target)) {
                throw UserProjectTemplateException(UserProjectTemplateFailure.RENAME_FAILED)
            }
            toItem(target)
        } catch (error: UserProjectTemplateException) {
            throw error
        } catch (error: Throwable) {
            throw UserProjectTemplateException(UserProjectTemplateFailure.RENAME_FAILED, error)
        }
    }

    fun exportTemplate(
        templatesDir: File,
        templateName: String,
        output: OutputStream,
    ): Boolean {
        return try {
            val root = ProjectPaths.ensureDir(templatesDir).canonicalFile
            val source = resolveExistingTemplate(root, templateName)
            source.inputStream().use { input ->
                input.copyTo(output)
            }
            true
        } catch (error: UserProjectTemplateException) {
            throw error
        } catch (error: Throwable) {
            throw UserProjectTemplateException(UserProjectTemplateFailure.EXPORT_FAILED, error)
        }
    }

    fun deleteTemplate(templatesDir: File, templateName: String): Boolean {
        return try {
            val root = ProjectPaths.ensureDir(templatesDir).canonicalFile
            val target = resolveExistingTemplate(root, templateName)
            if (!target.delete()) {
                throw UserProjectTemplateException(UserProjectTemplateFailure.DELETE_FAILED)
            }
            true
        } catch (error: UserProjectTemplateException) {
            throw error
        } catch (error: Throwable) {
            throw UserProjectTemplateException(UserProjectTemplateFailure.DELETE_FAILED, error)
        }
    }

    fun sanitizeTemplateFileName(sourceName: String?): String {
        val leafName = sourceName
            ?.replace('\\', '/')
            ?.substringAfterLast('/')
            ?.trim()
            .orEmpty()

        val baseName = leafName
            .removeZipExtension()
            .replace(invalidFileNameChars, "-")
            .replace(whitespace, " ")
            .trim(' ', '.', '-', '_')
            .ifBlank { "template" }

        return "$baseName.zip"
    }

    fun resolveUniqueTarget(templatesDir: File, sourceName: String?): File {
        val safeName = sanitizeTemplateFileName(sourceName)
        val baseName = safeName.removeZipExtension()
        var candidate = File(templatesDir, safeName)
        var index = 1
        while (candidate.exists()) {
            candidate = File(templatesDir, "$baseName-$index.zip")
            index++
        }
        return candidate
    }

    fun isZipFileName(fileName: String?): Boolean {
        return fileName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.equals("zip", ignoreCase = true) == true
    }

    fun formatTemplateSize(bytes: Long): String = when {
        bytes <= 0L -> "0 B"
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        bytes < 1024L * 1024L * 1024L -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024))
        else -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }

    private fun toItem(file: File): UserProjectTemplateItem = UserProjectTemplateItem(
        file = file,
        name = file.name,
        sizeBytes = file.length(),
        lastModifiedMillis = file.lastModified(),
        metadata = ProjectTemplateMetadataReader.readFromZip(file),
    )

    private fun resolveExistingTemplate(root: File, templateName: String): File {
        if (!isZipFileName(templateName)) {
            throw UserProjectTemplateException(UserProjectTemplateFailure.UNSAFE_PATH)
        }
        val target = File(root, templateName).canonicalFile
        if (!target.isInside(root) || !target.isFile || !isZipFileName(target.name)) {
            throw UserProjectTemplateException(UserProjectTemplateFailure.UNSAFE_PATH)
        }
        return target
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    null
                }
            }
            ?.takeUnless { it.isBlank() }
    }

    private fun String.removeZipExtension(): String {
        return if (endsWith(".zip", ignoreCase = true)) {
            dropLast(4)
        } else {
            this
        }
    }

    private fun File.isInside(root: File): Boolean {
        val rootPath = root.path.trimEnd(File.separatorChar)
        val targetPath = path
        return targetPath == rootPath || targetPath.startsWith(rootPath + File.separator)
    }
}
