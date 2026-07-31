package pl.tracelens.app.data

data class EraserPoint(
    val x: Float,
    val y: Float,
    val radius: Float,
)

data class ImageFilters(
    val grayscale: Boolean = false,
    val contour: Boolean = false,
    val contrast: Float = 1f,
    val invert: Boolean = false,
    val removeColor: Int? = null,
    val colorTolerance: Float = 0.08f,
    val autoRemoveBackground: Boolean = false,
    val simplifyLevels: Int = 0,
    val rotationDegrees: Float = 0f,
    val artifactCleanup: Int = 0,
    val eraserPoints: List<EraserPoint> = emptyList(),
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f,
)

data class LibraryImage(
    val id: String,
    val name: String,
    val originalFileName: String,
    val sourcePatternId: String? = null,
    val sourceContentSha256: String? = null,
    val contentSha256: String? = null,
    val processedFileName: String? = null,
    val favorite: Boolean = false,
    val collectionNames: List<String> = emptyList(),
    val filters: ImageFilters = ImageFilters(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = createdAt,
)

data class AppSettings(
    val themeMode: Int = 0,
    val tutorialCompleted: Boolean = false,
    val showIconHints: Boolean = true,
    val defaultOpacity: Float = 0.25f,
    val defaultGridEnabled: Boolean = false,
    val defaultGridDivisions: Int = 4,
    val defaultBlinkEnabled: Boolean = false,
    val defaultTimelapseEnabled: Boolean = false,
    val defaultTorchEnabled: Boolean = false,
    val defaultCameraId: String? = null,
    val showLevelIndicator: Boolean = true,
    val showMovementWarning: Boolean = true,
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
    val overlayBlendMode: Int = 0,
    val blinkIntervalMs: Long = 800L,
    val timelapseIntervalSeconds: Int = 30,
    val coverPhoto: String? = null,
    val tripodReferencePhoto: String? = null,
    val cameraId: String? = null,
    val orientation: Int = 0,
    val progressPhotos: List<String> = emptyList(),
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
)

data class StorageSummary(
    val imagesBytes: Long,
    val projectsBytes: Long,
    val cacheBytes: Long,
) {
    val totalBytes: Long get() = imagesBytes + projectsBytes + cacheBytes
}

data class ImportedProject(
    val image: LibraryImage,
    val project: TraceProject,
)
