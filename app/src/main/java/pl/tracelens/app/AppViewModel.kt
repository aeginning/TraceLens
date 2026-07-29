package pl.tracelens.app

import android.graphics.Bitmap
import android.net.Uri
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
import pl.tracelens.app.data.ImageFilters
import pl.tracelens.app.data.LibraryImage
import pl.tracelens.app.data.TraceProject
import pl.tracelens.app.image.ImageProcessor
import java.util.UUID

class AppViewModel(private val repository: AppRepository) : ViewModel() {
    var images by mutableStateOf(repository.loadImages().sortedImages())
        private set
    var projects by mutableStateOf(repository.loadProjects().sortedByDescending { it.modifiedAt })
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
            }.onFailure { message = it.message ?: "Nie udało się dodać obrazu." }
            busy = false
        }
    }

    fun toggleFavorite(image: LibraryImage) {
        updateImage(image.copy(favorite = !image.favorite))
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
                message = "Zapisano przetworzony obraz."
            }.onFailure { message = it.message ?: "Nie udało się przetworzyć obrazu." }
            busy = false
        }
    }

    fun bitmap(image: LibraryImage, processed: Boolean = true): Bitmap? =
        repository.loadBitmap(image, processed)

    fun createProject(name: String, image: LibraryImage): TraceProject {
        val project = TraceProject(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Nowy projekt" },
            imageId = image.id,
        )
        projects = listOf(project) + projects
        repository.saveProjects(projects)
        return project
    }

    fun updateProject(project: TraceProject) {
        val updated = project.copy(modifiedAt = System.currentTimeMillis())
        projects = projects.map { if (it.id == updated.id) updated else it }
            .sortedByDescending { it.modifiedAt }
        repository.saveProjects(projects)
    }

    fun addProgressPhoto(project: TraceProject, fileName: String): TraceProject {
        val updated = project.copy(progressPhotos = project.progressPhotos + fileName)
        updateProject(updated)
        return updated
    }

    fun imageForProject(project: TraceProject): LibraryImage? =
        images.firstOrNull { it.id == project.imageId }

    fun progressFile(projectId: String, fileName: String) =
        repository.fileForProgress(projectId, fileName)

    fun newProgressFile(projectId: String) = repository.progressFile(projectId)

    private fun updateImage(updated: LibraryImage) {
        images = images.map { if (it.id == updated.id) updated else it }.sortedImages()
        repository.saveImages(images)
    }
}

private fun List<LibraryImage>.sortedImages() =
    sortedWith(compareByDescending<LibraryImage> { it.favorite }.thenByDescending { it.createdAt })

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AppViewModel(repository) as T
}
