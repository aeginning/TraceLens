package pl.tracelens.app

import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.tracelens.app.data.AppRepository
import pl.tracelens.app.data.AppSettings
import pl.tracelens.app.data.ImageFilters
import pl.tracelens.app.data.LibraryImage
import pl.tracelens.app.data.TraceProject
import pl.tracelens.app.data.StorageSummary
import pl.tracelens.app.data.LocalTransferServer
import pl.tracelens.app.image.ImageProcessor
import pl.tracelens.app.image.StarterPattern
import java.util.UUID

class AppViewModel(private val repository: AppRepository) : ViewModel() {
    private val thumbnailCache = object : LruCache<String, Bitmap>(16 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }
    var images by mutableStateOf(repository.loadImages().sortedImages())
        private set
    var collectionNames by mutableStateOf(
        (repository.loadCollectionNames() + images.flatMap { it.collectionNames })
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
    )
        private set
    var projects by mutableStateOf(repository.loadProjects().sortedByDescending { it.modifiedAt })
        private set
    var appSettings by mutableStateOf(repository.loadSettings())
        private set
    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    fun consumeMessage() { message = null }

    fun importImage(uri: Uri, onComplete: (LibraryImage) -> Unit = {}) {
        viewModelScope.launch {
            busy = true
            runCatching {
                withContext(Dispatchers.IO) { repository.importImage(uri) }
            }.onSuccess { image ->
                images = (images + image).sortedImages()
                repository.saveImages(images)
                onComplete(image)
            }.onFailure { message = it.message ?: repository.text(R.string.operation_failed) }
            busy = false
        }
    }

    fun addStarterPattern(
        pattern: StarterPattern,
        localizedName: String,
        onComplete: (LibraryImage, Boolean) -> Unit = { _, _ -> },
    ) {
        viewModelScope.launch {
            busy = true
            val existing = withContext(Dispatchers.Default) {
                repository.findMatchingStarterPattern(pattern, images)
            }
            if (existing != null) {
                onComplete(existing, false)
                busy = false
                return@launch
            }
            runCatching {
                withContext(Dispatchers.Default) {
                    repository.importStarterPattern(pattern, localizedName)
                }
            }.onSuccess { image ->
                images = (images + image).sortedImages()
                repository.saveImages(images)
                onComplete(image, true)
            }.onFailure { message = it.message ?: repository.text(R.string.operation_failed) }
            busy = false
        }
    }

    fun toggleFavorite(image: LibraryImage) {
        updateImage(image.copy(favorite = !image.favorite))
    }

    fun renameImage(image: LibraryImage, newName: String) {
        val normalized = newName.trim()
            .replace(Regex("[\\\\/:*?\"<>|]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (normalized.isBlank()) {
            message = repository.text(R.string.image_name_empty)
            return
        }
        updateImage(image.copy(name = normalized))
        message = repository.text(R.string.image_renamed)
    }

    fun deleteImage(image: LibraryImage) {
        val linkedProjects = projects.filter { it.imageId == image.id }
        linkedProjects.forEach { repository.deleteProjectFiles(it.id) }
        projects = projects.filterNot { it.imageId == image.id }
        repository.saveProjects(projects)
        repository.deleteImageFiles(image)
        images = images.filterNot { it.id == image.id }.sortedImages()
        repository.saveImages(images)
        message = if (linkedProjects.isEmpty()) {
            repository.text(R.string.image_deleted, image.name)
        } else {
            repository.text(R.string.image_deleted_projects, image.name, linkedProjects.size)
        }
    }

    fun updateFilters(image: LibraryImage, filters: ImageFilters, onComplete: (LibraryImage) -> Unit = {}) {
        viewModelScope.launch {
            busy = true
            runCatching {
                withContext(Dispatchers.IO) {
                    val original = requireNotNull(repository.loadBitmap(image, processed = false))
                    val processed = ImageProcessor.process(original, filters)
                    repository.saveProcessed(image.copy(filters = filters), processed)
                }
            }.onSuccess {
                updateImage(it)
                onComplete(it)
            message = repository.text(R.string.save)
            }.onFailure { message = it.message ?: repository.text(R.string.operation_failed) }
            busy = false
        }
    }

    fun bitmap(image: LibraryImage, processed: Boolean = true): Bitmap? =
        repository.loadBitmap(image, processed)

    fun thumbnail(image: LibraryImage): Bitmap? {
        val key = "${image.id}:${image.processedFileName}:${image.filters.hashCode()}"
        return thumbnailCache[key] ?: repository.loadThumbnail(image)?.also {
            thumbnailCache.put(key, it)
        }
    }

    fun createProject(name: String, image: LibraryImage): TraceProject {
        val defaults = appSettings
        val project = TraceProject(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { repository.text(R.string.new_project_default) },
            imageId = image.id,
            opacity = defaults.defaultOpacity,
            gridEnabled = defaults.defaultGridEnabled,
            gridDivisions = defaults.defaultGridDivisions,
            cameraId = defaults.defaultCameraId,
        )
        projects = listOf(project) + projects
        repository.saveProjects(projects)
        markImageUsed(image.id)
        return project
    }

    fun updateSettings(settings: AppSettings) {
        appSettings = settings
        repository.saveSettings(settings)
    }

    fun updateProject(project: TraceProject) {
        val updated = project.copy(modifiedAt = System.currentTimeMillis())
        projects = projects.map { if (it.id == updated.id) updated else it }
            .sortedByDescending { it.modifiedAt }
        repository.saveProjects(projects)
    }

    fun renameProject(project: TraceProject, name: String) {
        val normalized = name.trim().replace(Regex("\\s+"), " ")
        if (normalized.isBlank()) {
            message = repository.text(R.string.project_name_empty)
            return
        }
        updateProject(project.copy(name = normalized))
        message = repository.text(R.string.project_renamed)
    }

    fun toggleProjectArchive(project: TraceProject) {
        updateProject(project.copy(archived = !project.archived))
        message = repository.text(if (project.archived) R.string.project_restored else R.string.project_archived)
    }

    fun updateImageCollections(image: LibraryImage, names: List<String>) {
        val normalized = names.map { it.trim().replace(Regex("\\s+"), " ") }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
        updateImage(image.copy(collectionNames = normalized))
        collectionNames = (collectionNames + normalized)
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
        repository.saveCollectionNames(collectionNames)
        message = repository.text(R.string.image_collections_saved)
    }

    fun createCollection(name: String): Boolean {
        val normalized = name.trim().replace(Regex("\\s+"), " ")
        if (normalized.isBlank()) {
            message = repository.text(R.string.collection_name_empty)
            return false
        }
        if (collectionNames.any { it.equals(normalized, ignoreCase = true) }) {
            message = repository.text(R.string.collection_exists)
            return false
        }
        collectionNames = (collectionNames + normalized).sortedBy { it.lowercase() }
        repository.saveCollectionNames(collectionNames)
        message = repository.text(R.string.collection_created, normalized)
        return true
    }

    fun markImageUsed(imageId: String) {
        images.firstOrNull { it.id == imageId }?.let {
            updateImage(it.copy(lastUsedAt = System.currentTimeMillis()))
        }
    }

    fun storageSummary(): StorageSummary = repository.storageSummary()

    fun cleanupStorage() {
        val freed = repository.cleanupOrphanedFiles(images, projects)
        message = if (freed > 0) {
            repository.text(R.string.cleanup_done, formatBytes(freed))
        } else {
            repository.text(R.string.cleanup_nothing)
        }
    }

    fun deleteProject(project: TraceProject) {
        repository.deleteProjectFiles(project.id)
        projects = projects.filterNot { it.id == project.id }
        repository.saveProjects(projects)
        message = repository.text(R.string.project_deleted, project.name)
    }

    fun exportProject(project: TraceProject, uri: Uri) {
        val image = imageForProject(project) ?: run {
            message = repository.text(R.string.project_image_missing)
            return
        }
        viewModelScope.launch {
            busy = true
            runCatching {
                withContext(Dispatchers.IO) { repository.exportProject(uri, project, image) }
            }.onSuccess {
                message = repository.text(R.string.project_exported, project.name)
            }.onFailure {
                message = it.message ?: repository.text(R.string.operation_failed)
            }
            busy = false
        }
    }

    fun shareProject(project: TraceProject, onReady: (Uri) -> Unit) {
        val image = imageForProject(project) ?: run {
            message = repository.text(R.string.project_image_missing)
            return
        }
        viewModelScope.launch {
            busy = true
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.createShareableProjectArchive(project, image)
                }
            }.onSuccess(onReady).onFailure {
                message = it.message ?: repository.text(R.string.operation_failed)
            }
            busy = false
        }
    }

    fun prepareLocalTransfer(project: TraceProject, onReady: (LocalTransferServer) -> Unit) {
        val image = imageForProject(project) ?: return
        viewModelScope.launch {
            busy = true
            runCatching {
                withContext(Dispatchers.IO) { repository.createLocalTransfer(project, image) }
            }.onSuccess(onReady).onFailure {
                message = it.message ?: repository.text(R.string.operation_failed)
            }
            busy = false
        }
    }

    fun importProject(uri: Uri, onComplete: (TraceProject) -> Unit = {}) {
        viewModelScope.launch {
            busy = true
            runCatching {
                withContext(Dispatchers.IO) { repository.importProject(uri) }
            }.onSuccess { imported ->
                images = (images + imported.image).sortedImages()
                projects = (listOf(imported.project) + projects).sortedByDescending { it.modifiedAt }
                collectionNames = (collectionNames + imported.image.collectionNames)
                    .distinctBy { it.lowercase() }
                    .sortedBy { it.lowercase() }
                repository.saveImages(images)
                repository.saveProjects(projects)
                repository.saveCollectionNames(collectionNames)
                message = repository.text(R.string.project_imported, imported.project.name)
                onComplete(imported.project)
            }.onFailure {
                message = it.message ?: repository.text(R.string.operation_failed)
            }
            busy = false
        }
    }

    fun addProgressPhoto(project: TraceProject, fileName: String): TraceProject {
        val updated = project.copy(progressPhotos = project.progressPhotos + fileName)
        updateProject(updated)
        return updated
    }

    fun deleteProgressPhoto(project: TraceProject, fileName: String): TraceProject {
        repository.deleteProgressPhoto(project.id, fileName)
        val remaining = project.progressPhotos.filterNot { it == fileName }
        val updated = project.copy(
            progressPhotos = remaining,
            coverPhoto = if (project.coverPhoto == fileName) remaining.lastOrNull() else project.coverPhoto,
        )
        updateProject(updated)
        message = repository.text(R.string.progress_photo_deleted)
        return updated
    }

    fun setProjectCover(project: TraceProject, fileName: String): TraceProject {
        val updated = project.copy(coverPhoto = fileName)
        updateProject(updated)
        message = repository.text(R.string.project_cover_set)
        return updated
    }

    fun setTripodReference(project: TraceProject, fileName: String): TraceProject {
        val updated = project.copy(tripodReferencePhoto = fileName)
        updateProject(updated)
        message = repository.text(R.string.tripod_reference_saved)
        return updated
    }

    fun imageForProject(project: TraceProject): LibraryImage? =
        images.firstOrNull { it.id == project.imageId }

    fun progressFile(projectId: String, fileName: String) =
        repository.fileForProgress(projectId, fileName)

    fun progressBitmap(projectId: String, fileName: String, maxSize: Int = 1800): Bitmap? =
        repository.loadProgressBitmap(projectId, fileName, maxSize)

    fun progressPhotoUri(projectId: String, fileName: String) =
        repository.progressPhotoUri(projectId, fileName)

    fun newProgressFile(projectId: String) = repository.progressFile(projectId)

    fun publishProgressPhoto(projectName: String, file: java.io.File): Boolean =
        repository.publishProgressPhoto(projectName, file)

    fun exportPreparedImage(
        image: LibraryImage,
        filters: ImageFilters,
        onComplete: (String) -> Unit,
    ) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val original = requireNotNull(repository.loadBitmap(image, processed = false))
                    repository.publishPreparedImage(image.name, ImageProcessor.process(original, filters))
                }
            }
            onComplete(if (result.isSuccess) {
                repository.text(R.string.prepared_image_exported)
            } else {
                result.exceptionOrNull()?.message ?: repository.text(R.string.operation_failed)
            })
        }
    }

    private fun updateImage(updated: LibraryImage) {
        images = images.map { if (it.id == updated.id) updated else it }.sortedImages()
        repository.saveImages(images)
    }
}

private fun List<LibraryImage>.sortedImages() =
    sortedWith(compareByDescending<LibraryImage> { it.favorite }.thenByDescending { it.createdAt })

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L * 1024L -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    bytes >= 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AppViewModel(repository) as T
}
