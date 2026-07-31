package pl.tracelens.app.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.core.content.FileProvider
import pl.tracelens.app.R
import pl.tracelens.app.image.StarterPattern
import pl.tracelens.app.image.StarterPatterns
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class AppRepository(private val context: Context) {
    fun text(id: Int, vararg args: Any): String = context.getString(id, *args)
    private val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
    private val projectsDir = File(context.filesDir, "projects").apply { mkdirs() }
    private val metadataFile = File(context.filesDir, "library.json")
    private val projectsFile = File(context.filesDir, "projects.json")
    private val preferences = context.getSharedPreferences("trace_lens_settings", Context.MODE_PRIVATE)

    fun loadSettings() = AppSettings(
        themeMode = preferences.getInt("themeMode", 0).coerceIn(0, 2),
        tutorialCompleted = preferences.getBoolean("tutorialCompleted", false),
        showIconHints = preferences.getBoolean("showIconHints", true),
        defaultOpacity = preferences.getFloat("defaultOpacity", 0.25f).coerceIn(0f, 1f),
        defaultGridEnabled = preferences.getBoolean("defaultGridEnabled", false),
        defaultGridDivisions = preferences.getInt("defaultGridDivisions", 4).coerceIn(2, 12),
        defaultBlinkEnabled = preferences.getBoolean("defaultBlinkEnabled", false),
        defaultTimelapseEnabled = preferences.getBoolean("defaultTimelapseEnabled", false),
        defaultTorchEnabled = preferences.getBoolean("defaultTorchEnabled", false),
        defaultCameraId = preferences.getString("defaultCameraId", null),
        showLevelIndicator = preferences.getBoolean("showLevelIndicator", true),
        showMovementWarning = preferences.getBoolean("showMovementWarning", true),
    )

    fun saveSettings(settings: AppSettings) {
        preferences.edit()
            .putInt("themeMode", settings.themeMode)
            .putBoolean("tutorialCompleted", settings.tutorialCompleted)
            .putBoolean("showIconHints", settings.showIconHints)
            .putFloat("defaultOpacity", settings.defaultOpacity)
            .putBoolean("defaultGridEnabled", settings.defaultGridEnabled)
            .putInt("defaultGridDivisions", settings.defaultGridDivisions)
            .putBoolean("defaultBlinkEnabled", settings.defaultBlinkEnabled)
            .putBoolean("defaultTimelapseEnabled", settings.defaultTimelapseEnabled)
            .putBoolean("defaultTorchEnabled", settings.defaultTorchEnabled)
            .putString("defaultCameraId", settings.defaultCameraId)
            .putBoolean("showLevelIndicator", settings.showLevelIndicator)
            .putBoolean("showMovementWarning", settings.showMovementWarning)
            .apply()
    }

    fun loadImages(): List<LibraryImage> = runCatching {
        if (!metadataFile.exists()) return emptyList()
        val array = JSONArray(metadataFile.readText())
        buildList {
            for (i in 0 until array.length()) add(array.getJSONObject(i).toLibraryImage())
        }
    }.getOrDefault(emptyList())

    fun saveImages(images: List<LibraryImage>) {
        val array = JSONArray()
        images.forEach { array.put(it.toJson()) }
        metadataFile.writeText(array.toString(2))
    }

    fun loadCollectionNames(): List<String> =
        preferences.getStringSet("collectionNames", emptySet()).orEmpty()
            .sortedBy { it.lowercase() }

    fun saveCollectionNames(names: List<String>) {
        preferences.edit().putStringSet("collectionNames", names.toSet()).apply()
    }

    fun loadProjects(): List<TraceProject> = runCatching {
        if (!projectsFile.exists()) return emptyList()
        val array = JSONArray(projectsFile.readText())
        buildList {
            for (i in 0 until array.length()) add(array.getJSONObject(i).toTraceProject())
        }
    }.getOrDefault(emptyList())

    fun saveProjects(projects: List<TraceProject>) {
        val array = JSONArray()
        projects.forEach { array.put(it.toJson()) }
        projectsFile.writeText(array.toString(2))
    }

    fun storageSummary() = StorageSummary(
        imagesBytes = imagesDir.recursiveSize(),
        projectsBytes = projectsDir.recursiveSize(),
        cacheBytes = context.cacheDir.recursiveSize(),
    )

    fun cleanupOrphanedFiles(images: List<LibraryImage>, projects: List<TraceProject>): Long {
        val before = storageSummary().totalBytes
        val validImageFiles = images.flatMap {
            listOfNotNull(it.originalFileName, it.processedFileName)
        }.toSet()
        imagesDir.listFiles()?.filter { it.isFile && it.name !in validImageFiles }?.forEach {
            it.delete()
        }
        val validProjectIds = projects.map { it.id }.toSet()
        projectsDir.listFiles()?.filter { it.isDirectory && it.name !in validProjectIds }?.forEach {
            it.deleteRecursively()
        }
        context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
        return (before - storageSummary().totalBytes).coerceAtLeast(0L)
    }

    fun importImage(uri: Uri): LibraryImage {
        val id = UUID.randomUUID().toString()
        val displayName = queryDisplayName(uri) ?: text(R.string.default_image_name, id.take(6))
        val extension = displayName.substringAfterLast('.', "png").lowercase()
            .takeIf { it in setOf("png", "jpg", "jpeg") } ?: "png"
        val fileName = "$id.$extension"
        val output = File(imagesDir, fileName)
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { text(R.string.cannot_open_image) }
            output.outputStream().use(input::copyTo)
        }
        return LibraryImage(id, displayName, fileName)
    }

    fun importGeneratedImage(name: String, bitmap: Bitmap): LibraryImage {
        val id = UUID.randomUUID().toString()
        val fileName = "$id.png"
        FileOutputStream(File(imagesDir, fileName)).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return LibraryImage(id = id, name = name, originalFileName = fileName)
    }

    fun importStarterPattern(pattern: StarterPattern, name: String): LibraryImage {
        val bitmap = requireNotNull(StarterPatterns.bitmap(context, pattern)) {
            text(R.string.cannot_open_image)
        }
        return importGeneratedImage(name, bitmap).copy(sourcePatternId = pattern.id)
    }

    fun findMatchingStarterPattern(
        pattern: StarterPattern,
        images: List<LibraryImage>,
    ): LibraryImage? {
        val patternBitmap = StarterPatterns.bitmap(context, pattern) ?: return null
        return images.asSequence()
            .filter { it.sourcePatternId == pattern.id }
            .firstOrNull { image ->
            val currentBitmap = loadBitmap(image) ?: return@firstOrNull false
            currentBitmap.sameAs(patternBitmap)
        }
    }

    fun imageFile(image: LibraryImage, processed: Boolean = true): File {
        val name = if (processed) image.processedFileName ?: image.originalFileName else image.originalFileName
        return File(imagesDir, name)
    }

    fun loadBitmap(image: LibraryImage, processed: Boolean = true): Bitmap? {
        val file = imageFile(image, processed)
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
        if (processed && image.processedFileName != null) return bitmap
        return applyExifOrientation(file, bitmap)
    }

    fun loadThumbnail(image: LibraryImage, maxSize: Int = 360): Bitmap? {
        val path = imageFile(image).absolutePath
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sampleSize = 1
        while (bounds.outWidth / sampleSize > maxSize * 2 ||
            bounds.outHeight / sampleSize > maxSize * 2
        ) {
            sampleSize *= 2
        }
        val bitmap = BitmapFactory.decodeFile(
            path,
            BitmapFactory.Options().apply { inSampleSize = sampleSize },
        ) ?: return null
        return if (image.processedFileName != null) bitmap
        else applyExifOrientation(imageFile(image, processed = false), bitmap)
    }

    fun saveProcessed(image: LibraryImage, bitmap: Bitmap): LibraryImage {
        val fileName = "${image.id}_processed.png"
        FileOutputStream(File(imagesDir, fileName)).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return image.copy(processedFileName = fileName)
    }

    fun deleteImageFiles(image: LibraryImage) {
        imageFile(image, processed = false).delete()
        image.processedFileName?.let { File(imagesDir, it).delete() }
    }

    fun progressDirectory(projectId: String): File =
        File(projectsDir, "$projectId/progress").apply { mkdirs() }

    fun progressFile(projectId: String): File =
        File(progressDirectory(projectId), "progress_${System.currentTimeMillis()}.jpg")

    fun fileForProgress(projectId: String, fileName: String): File =
        File(progressDirectory(projectId), fileName)

    fun loadProgressBitmap(projectId: String, fileName: String, maxSize: Int = 1800): Bitmap? {
        val file = fileForProgress(projectId, fileName)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        var sampleSize = 1
        while (bounds.outWidth / sampleSize > maxSize * 2 ||
            bounds.outHeight / sampleSize > maxSize * 2
        ) {
            sampleSize *= 2
        }
        val bitmap = BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sampleSize },
        ) ?: return null
        return applyExifOrientation(file, bitmap)
    }

    private fun applyExifOrientation(file: File, bitmap: Bitmap): Bitmap {
        val exif = runCatching { ExifInterface(file) }.getOrNull() ?: return bitmap
        val matrix = Matrix().apply {
            if (exif.isFlipped) postScale(-1f, 1f)
            if (exif.rotationDegrees != 0) postRotate(exif.rotationDegrees.toFloat())
        }
        if (!exif.isFlipped && exif.rotationDegrees == 0) return bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            .also { if (it !== bitmap) bitmap.recycle() }
    }

    fun progressPhotoUri(projectId: String, fileName: String): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.files",
            fileForProgress(projectId, fileName),
        )

    fun deleteProgressPhoto(projectId: String, fileName: String): Boolean =
        fileForProgress(projectId, fileName).delete()

    fun publishProgressPhoto(projectName: String, source: File): Boolean {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, source.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/TraceLens/${projectName.toSafeFolderName()}",
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        return try {
            resolver.openOutputStream(uri)?.use { output ->
                source.inputStream().use { input -> input.copyTo(output) }
            } ?: error(text(R.string.cannot_open_gallery_file))
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            true
        } catch (_: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    }

    fun publishPreparedImage(name: String, bitmap: Bitmap) {
        val safeName = name.substringBeforeLast('.').toSafeFolderName()
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${safeName}_TraceLens.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/TraceLens/Obrazy/")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error(text(R.string.cannot_create_gallery_file))
        try {
            resolver.openOutputStream(uri)?.use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
            } ?: error(text(R.string.cannot_open_gallery_file))
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            check(resolver.update(uri, values, null, null) > 0) {
                text(R.string.cannot_finish_gallery_save)
            }
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    fun deleteProjectFiles(projectId: String): Boolean {
        val directory = File(projectsDir, projectId)
        return !directory.exists() || directory.deleteRecursively()
    }

    fun exportProject(uri: Uri, project: TraceProject, image: LibraryImage) {
        val output = context.contentResolver.openOutputStream(uri)
            ?: error(text(R.string.cannot_open_destination))
        ZipOutputStream(output.buffered()).use { zip ->
            val manifest = JSONObject().apply {
                put("format", "TraceLensProject")
                put("formatVersion", 1)
                put("exportedAt", System.currentTimeMillis())
                put("image", image.toJson())
                put("project", project.toJson())
            }
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(manifest.toString(2).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            addFileToZip(zip, imageFile(image, processed = false), "image/original")
            image.processedFileName?.let {
                addFileToZip(zip, File(imagesDir, it), "image/processed")
            }
            val projectDirectory = File(projectsDir, project.id)
            if (projectDirectory.exists()) {
                projectDirectory.walkTopDown().filter { it.isFile }.forEach { file ->
                    val relative = file.relativeTo(projectDirectory).invariantSeparatorsPath
                    addFileToZip(zip, file, "project-files/$relative")
                }
            }
        }
    }

    fun createShareableProjectArchive(project: TraceProject, image: LibraryImage): Uri {
        val directory = File(context.cacheDir, "project-archives").apply { mkdirs() }
        val file = File(directory, "${project.name.toSafeFolderName()}.tracelens")
        file.outputStream().use { output ->
            ZipOutputStream(output.buffered()).use { zip ->
                val manifest = JSONObject().apply {
                    put("format", "TraceLensProject")
                    put("formatVersion", 1)
                    put("exportedAt", System.currentTimeMillis())
                    put("image", image.toJson())
                    put("project", project.toJson())
                }
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(manifest.toString(2).toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                addFileToZip(zip, imageFile(image, processed = false), "image/original")
                image.processedFileName?.let {
                    addFileToZip(zip, File(imagesDir, it), "image/processed")
                }
                val projectDirectory = File(projectsDir, project.id)
                if (projectDirectory.exists()) {
                    projectDirectory.walkTopDown().filter { it.isFile }.forEach { source ->
                        val relative = source.relativeTo(projectDirectory).invariantSeparatorsPath
                        addFileToZip(zip, source, "project-files/$relative")
                    }
                }
            }
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    }

    fun createLocalTransfer(project: TraceProject, image: LibraryImage): LocalTransferServer {
        createShareableProjectArchive(project, image)
        val file = File(
            File(context.cacheDir, "project-archives"),
            "${project.name.toSafeFolderName()}.tracelens",
        )
        return LocalTransferServer(file, text(R.string.connect_wifi))
    }

    fun importProject(uri: Uri): ImportedProject {
        val temporary = File(context.cacheDir, "project-import-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: error(text(R.string.cannot_open_archive))
            var extractedBytes = 0L
            ZipInputStream(input.buffered()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val target = File(temporary, entry.name)
                    val rootPath = temporary.canonicalPath + File.separator
                    require(target.canonicalPath.startsWith(rootPath)) { text(R.string.invalid_archive_path) }
                    if (entry.isDirectory) {
                        target.mkdirs()
                    } else {
                        target.parentFile?.mkdirs()
                        target.outputStream().use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var read = zip.read(buffer)
                            while (read >= 0) {
                                if (read > 0) {
                                    extractedBytes += read
                                    require(extractedBytes <= 500L * 1024L * 1024L) {
                                        text(R.string.archive_too_large)
                                    }
                                    output.write(buffer, 0, read)
                                }
                                read = zip.read(buffer)
                            }
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
            val manifestFile = File(temporary, "manifest.json")
            require(manifestFile.isFile) { text(R.string.archive_missing_manifest) }
            val manifest = JSONObject(manifestFile.readText())
            require(manifest.optString("format") == "TraceLensProject") { "To nie jest archiwum TraceLens." }
            require(manifest.optInt("formatVersion") == 1) { text(R.string.unsupported_archive) }

            val sourceImage = manifest.getJSONObject("image").toLibraryImage()
            val sourceProject = manifest.getJSONObject("project").toTraceProject()
            val originalSource = File(temporary, "image/original")
            require(originalSource.isFile) { text(R.string.archive_missing_source) }

            val newImageId = UUID.randomUUID().toString()
            val extension = sourceImage.originalFileName.substringAfterLast('.', "png")
                .takeIf { it.matches(Regex("[A-Za-z0-9]{1,5}")) } ?: "png"
            val originalName = "$newImageId.$extension"
            originalSource.copyTo(File(imagesDir, originalName))
            val processedSource = File(temporary, "image/processed")
            val processedName = if (processedSource.isFile) "${newImageId}_processed.png" else null
            if (processedName != null) processedSource.copyTo(File(imagesDir, processedName))

            val newProjectId = UUID.randomUUID().toString()
            val importedProjectDirectory = File(temporary, "project-files")
            if (importedProjectDirectory.exists()) {
                importedProjectDirectory.copyRecursively(File(projectsDir, newProjectId), overwrite = true)
            }
            val now = System.currentTimeMillis()
            val image = sourceImage.copy(
                id = newImageId,
                name = "${sourceImage.name} (import)",
                originalFileName = originalName,
                processedFileName = processedName,
                favorite = false,
                createdAt = now,
                lastUsedAt = now,
            )
            val project = sourceProject.copy(
                id = newProjectId,
                name = "${sourceProject.name} (import)",
                imageId = newImageId,
                archived = false,
                createdAt = now,
                modifiedAt = now,
            )
            return ImportedProject(image, project)
        } finally {
            temporary.deleteRecursively()
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
        return context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    private fun addFileToZip(zip: ZipOutputStream, file: File, entryName: String) {
        if (!file.isFile) return
        zip.putNextEntry(ZipEntry(entryName))
        file.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }
}

private fun String.toSafeFolderName(): String {
    val sanitized = trim().replace(Regex("""[\\/:*?"<>|]"""), "_").trim('.', ' ')
    return sanitized.ifBlank { "Projekt" }
}

private fun LibraryImage.toJson() = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("originalFileName", originalFileName)
    put("sourcePatternId", sourcePatternId)
    put("processedFileName", processedFileName)
    put("favorite", favorite)
    put("collectionNames", JSONArray(collectionNames))
    put("lastUsedAt", lastUsedAt)
    put("createdAt", createdAt)
    put("filters", JSONObject().apply {
        put("grayscale", filters.grayscale)
        put("contour", filters.contour)
        put("contrast", filters.contrast.toDouble())
        put("invert", filters.invert)
        put("removeColor", filters.removeColor)
        put("colorTolerance", filters.colorTolerance.toDouble())
        put("autoRemoveBackground", filters.autoRemoveBackground)
        put("simplifyLevels", filters.simplifyLevels)
        put("rotationDegrees", filters.rotationDegrees.toDouble())
        put("artifactCleanup", filters.artifactCleanup)
        put("eraserPoints", JSONArray().apply {
            filters.eraserPoints.forEach { point ->
                put(JSONObject().apply {
                    put("x", point.x.toDouble())
                    put("y", point.y.toDouble())
                    put("radius", point.radius.toDouble())
                })
            }
        })
        put("cropLeft", filters.cropLeft.toDouble())
        put("cropTop", filters.cropTop.toDouble())
        put("cropRight", filters.cropRight.toDouble())
        put("cropBottom", filters.cropBottom.toDouble())
    })
}

private fun JSONObject.toLibraryImage(): LibraryImage {
    val filterJson = optJSONObject("filters") ?: JSONObject()
    val eraserJson = filterJson.optJSONArray("eraserPoints") ?: JSONArray()
    val collectionsJson = optJSONArray("collectionNames") ?: JSONArray()
    return LibraryImage(
        id = getString("id"),
        name = getString("name"),
        originalFileName = getString("originalFileName"),
        sourcePatternId = optString("sourcePatternId")
            .takeIf { it.isNotBlank() && it != "null" },
        processedFileName = optString("processedFileName").takeIf { it.isNotBlank() && it != "null" },
        favorite = optBoolean("favorite"),
        collectionNames = buildList {
            for (index in 0 until collectionsJson.length()) add(collectionsJson.getString(index))
        },
        filters = ImageFilters(
            grayscale = filterJson.optBoolean("grayscale"),
            contour = filterJson.optBoolean("contour"),
            contrast = filterJson.optDouble("contrast", 1.0).toFloat(),
            invert = filterJson.optBoolean("invert"),
            removeColor = if (filterJson.has("removeColor") && !filterJson.isNull("removeColor")) {
                filterJson.getInt("removeColor")
            } else null,
            colorTolerance = filterJson.optDouble("colorTolerance", 0.08).toFloat(),
            autoRemoveBackground = filterJson.optBoolean("autoRemoveBackground"),
            simplifyLevels = filterJson.optInt("simplifyLevels").let {
                if (it == 0) 0 else it.coerceIn(2, 16)
            },
            rotationDegrees = filterJson.optDouble("rotationDegrees", 0.0).toFloat()
                .coerceIn(-180f, 180f),
            artifactCleanup = filterJson.optInt("artifactCleanup", 0).coerceIn(0, 100),
            eraserPoints = buildList {
                for (index in 0 until eraserJson.length()) {
                    val point = eraserJson.getJSONObject(index)
                    add(
                        EraserPoint(
                            x = point.optDouble("x").toFloat().coerceIn(0f, 1f),
                            y = point.optDouble("y").toFloat().coerceIn(0f, 1f),
                            radius = point.optDouble("radius", 0.03).toFloat().coerceIn(0.005f, 0.25f),
                        )
                    )
                }
            },
            cropLeft = filterJson.optDouble("cropLeft", 0.0).toFloat(),
            cropTop = filterJson.optDouble("cropTop", 0.0).toFloat(),
            cropRight = filterJson.optDouble("cropRight", 1.0).toFloat(),
            cropBottom = filterJson.optDouble("cropBottom", 1.0).toFloat(),
        ),
        createdAt = optLong("createdAt", System.currentTimeMillis()),
        lastUsedAt = optLong("lastUsedAt", optLong("createdAt", System.currentTimeMillis())),
    )
}

private fun TraceProject.toJson() = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("imageId", imageId)
    put("offsetX", offsetX.toDouble())
    put("offsetY", offsetY.toDouble())
    put("scale", scale.toDouble())
    put("rotation", rotation.toDouble())
    put("mirrorX", mirrorX)
    put("mirrorY", mirrorY)
    put("opacity", opacity.toDouble())
    put("gridEnabled", gridEnabled)
    put("gridDivisions", gridDivisions)
    put("overlayBlendMode", overlayBlendMode)
    put("blinkIntervalMs", blinkIntervalMs)
    put("timelapseIntervalSeconds", timelapseIntervalSeconds)
    put("coverPhoto", coverPhoto)
    put("tripodReferencePhoto", tripodReferencePhoto)
    put("cameraId", cameraId)
    put("orientation", orientation)
    put("createdAt", createdAt)
    put("modifiedAt", modifiedAt)
    put("progressPhotos", JSONArray(progressPhotos))
    put("archived", archived)
}

private fun JSONObject.toTraceProject(): TraceProject {
    val photos = optJSONArray("progressPhotos") ?: JSONArray()
    return TraceProject(
        id = getString("id"),
        name = getString("name"),
        imageId = getString("imageId"),
        offsetX = optDouble("offsetX").toFloat(),
        offsetY = optDouble("offsetY").toFloat(),
        scale = optDouble("scale", 1.0).toFloat(),
        rotation = optDouble("rotation").toFloat(),
        mirrorX = optBoolean("mirrorX"),
        mirrorY = optBoolean("mirrorY"),
        opacity = optDouble("opacity", 0.25).toFloat(),
        gridEnabled = optBoolean("gridEnabled"),
        gridDivisions = optInt("gridDivisions", 4).coerceIn(2, 12),
        overlayBlendMode = optInt("overlayBlendMode").coerceIn(0, 2),
        blinkIntervalMs = optLong("blinkIntervalMs", 800L).coerceIn(250L, 2_000L),
        timelapseIntervalSeconds = optInt("timelapseIntervalSeconds", 30).coerceIn(5, 3600),
        coverPhoto = optString("coverPhoto").takeIf { it.isNotBlank() && it != "null" },
        tripodReferencePhoto = optString("tripodReferencePhoto").takeIf { it.isNotBlank() && it != "null" },
        cameraId = optString("cameraId").takeIf { it.isNotBlank() && it != "null" },
        orientation = optInt("orientation"),
        progressPhotos = buildList { for (i in 0 until photos.length()) add(photos.getString(i)) },
        archived = optBoolean("archived"),
        createdAt = optLong("createdAt", System.currentTimeMillis()),
        modifiedAt = optLong("modifiedAt", System.currentTimeMillis()),
    )
}

private fun File.recursiveSize(): Long =
    if (!exists()) 0L else if (isFile) length() else listFiles()?.sumOf { it.recursiveSize() } ?: 0L
