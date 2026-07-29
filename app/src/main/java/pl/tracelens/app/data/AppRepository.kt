package pl.tracelens.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AppRepository(private val context: Context) {
    private val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
    private val projectsDir = File(context.filesDir, "projects").apply { mkdirs() }
    private val metadataFile = File(context.filesDir, "library.json")
    private val projectsFile = File(context.filesDir, "projects.json")

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

    fun importImage(uri: Uri): LibraryImage {
        val id = UUID.randomUUID().toString()
        val displayName = queryDisplayName(uri) ?: "Obraz ${id.take(6)}"
        val extension = displayName.substringAfterLast('.', "png").lowercase()
            .takeIf { it in setOf("png", "jpg", "jpeg") } ?: "png"
        val fileName = "$id.$extension"
        val output = File(imagesDir, fileName)
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Nie można otworzyć obrazu." }
            output.outputStream().use(input::copyTo)
        }
        return LibraryImage(id, displayName, fileName)
    }

    fun imageFile(image: LibraryImage, processed: Boolean = true): File {
        val name = if (processed) image.processedFileName ?: image.originalFileName else image.originalFileName
        return File(imagesDir, name)
    }

    fun loadBitmap(image: LibraryImage, processed: Boolean = true): Bitmap? =
        BitmapFactory.decodeFile(imageFile(image, processed).absolutePath)

    fun saveProcessed(image: LibraryImage, bitmap: Bitmap): LibraryImage {
        val fileName = "${image.id}_processed.png"
        FileOutputStream(File(imagesDir, fileName)).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return image.copy(processedFileName = fileName)
    }

    fun progressDirectory(projectId: String): File =
        File(projectsDir, "$projectId/progress").apply { mkdirs() }

    fun progressFile(projectId: String): File =
        File(progressDirectory(projectId), "progress_${System.currentTimeMillis()}.jpg")

    fun fileForProgress(projectId: String, fileName: String): File =
        File(progressDirectory(projectId), fileName)

    fun deleteProgressPhoto(projectId: String, fileName: String): Boolean =
        fileForProgress(projectId, fileName).delete()

    fun deleteProjectFiles(projectId: String): Boolean {
        val directory = File(projectsDir, projectId)
        return !directory.exists() || directory.deleteRecursively()
    }

    private fun queryDisplayName(uri: Uri): String? {
        val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
        return context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }
}

private fun LibraryImage.toJson() = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("originalFileName", originalFileName)
    put("processedFileName", processedFileName)
    put("favorite", favorite)
    put("createdAt", createdAt)
    put("filters", JSONObject().apply {
        put("grayscale", filters.grayscale)
        put("contour", filters.contour)
        put("contrast", filters.contrast.toDouble())
        put("invert", filters.invert)
        put("removeColor", filters.removeColor)
        put("colorTolerance", filters.colorTolerance.toDouble())
        put("cropLeft", filters.cropLeft.toDouble())
        put("cropTop", filters.cropTop.toDouble())
        put("cropRight", filters.cropRight.toDouble())
        put("cropBottom", filters.cropBottom.toDouble())
    })
}

private fun JSONObject.toLibraryImage(): LibraryImage {
    val filterJson = optJSONObject("filters") ?: JSONObject()
    return LibraryImage(
        id = getString("id"),
        name = getString("name"),
        originalFileName = getString("originalFileName"),
        processedFileName = optString("processedFileName").takeIf { it.isNotBlank() && it != "null" },
        favorite = optBoolean("favorite"),
        filters = ImageFilters(
            grayscale = filterJson.optBoolean("grayscale"),
            contour = filterJson.optBoolean("contour"),
            contrast = filterJson.optDouble("contrast", 1.0).toFloat(),
            invert = filterJson.optBoolean("invert"),
            removeColor = if (filterJson.has("removeColor") && !filterJson.isNull("removeColor")) {
                filterJson.getInt("removeColor")
            } else null,
            colorTolerance = filterJson.optDouble("colorTolerance", 0.08).toFloat(),
            cropLeft = filterJson.optDouble("cropLeft", 0.0).toFloat(),
            cropTop = filterJson.optDouble("cropTop", 0.0).toFloat(),
            cropRight = filterJson.optDouble("cropRight", 1.0).toFloat(),
            cropBottom = filterJson.optDouble("cropBottom", 1.0).toFloat(),
        ),
        createdAt = optLong("createdAt", System.currentTimeMillis()),
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
    put("cameraId", cameraId)
    put("orientation", orientation)
    put("createdAt", createdAt)
    put("modifiedAt", modifiedAt)
    put("progressPhotos", JSONArray(progressPhotos))
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
        cameraId = optString("cameraId").takeIf { it.isNotBlank() && it != "null" },
        orientation = optInt("orientation"),
        progressPhotos = buildList { for (i in 0 until photos.length()) add(photos.getString(i)) },
        createdAt = optLong("createdAt", System.currentTimeMillis()),
        modifiedAt = optLong("modifiedAt", System.currentTimeMillis()),
    )
}
