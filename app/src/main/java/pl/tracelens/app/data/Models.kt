package pl.tracelens.app.data

data class ImageFilters(
    val grayscale: Boolean = false,
    val contour: Boolean = false,
    val contrast: Float = 1f,
    val invert: Boolean = false,
    val removeColor: Int? = null,
    val colorTolerance: Float = 0.08f,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f,
)

data class LibraryImage(
    val id: String,
    val name: String,
    val originalFileName: String,
    val processedFileName: String? = null,
    val favorite: Boolean = false,
    val filters: ImageFilters = ImageFilters(),
    val createdAt: Long = System.currentTimeMillis(),
)

data class TraceProject(
    val id: String,
    val name: String,
    val imageId: String,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val mirrorX: Boolean = false,
    val mirrorY: Boolean = false,
    val opacity: Float = 0.25f,
    val gridEnabled: Boolean = false,
    val gridDivisions: Int = 4,
    val overlayTint: Int? = null,
    val overlayBlendMode: Int = 0,
    val blinkIntervalMs: Long = 800L,
    val cameraId: String? = null,
    val orientation: Int = 0,
    val progressPhotos: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
)
