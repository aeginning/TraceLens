@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package pl.tracelens.app.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Rect
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

internal object SocialExports {
    private const val PURPLE = 0xFF7C4DFF.toInt()
    private const val DARK = 0xFF16131D.toInt()

    suspend fun createCollage(
        context: Context,
        projectName: String,
        photos: List<Bitmap>,
    ): Uri = withContext(Dispatchers.IO) {
        require(photos.isNotEmpty())
        val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(DARK)
        val columns = 2
        val rows = (photos.size + columns - 1) / columns
        val footerHeight = 150f
        val gap = 8f
        val cellWidth = (1080f - gap * (columns + 1)) / columns
        val cellHeight = (1350f - footerHeight - gap * (rows + 1)) / rows
        photos.forEachIndexed { index, photo ->
            val column = index % columns
            val row = index / columns
            val rect = RectF(
                gap + column * (cellWidth + gap),
                gap + row * (cellHeight + gap),
                gap + column * (cellWidth + gap) + cellWidth,
                gap + row * (cellHeight + gap) + cellHeight,
            )
            drawCenterCrop(canvas, photo, rect)
        }
        drawBranding(canvas, projectName, 1200f, 1350f)
        saveImage(context, projectName, "collage", bitmap).also { bitmap.recycle() }
    }

    suspend fun createReferenceResult(
        context: Context,
        projectName: String,
        reference: Bitmap,
        result: Bitmap,
        story: Boolean,
        referenceLabel: String,
        resultLabel: String,
    ): Uri = withContext(Dispatchers.IO) {
        val width = 1080
        val height = if (story) 1920 else 1350
        val footerTop = height - 150f
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(DARK)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 38f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        if (story) {
            val half = footerTop / 2f
            drawCenterCrop(canvas, reference, RectF(0f, 0f, width.toFloat(), half))
            drawCenterCrop(canvas, result, RectF(0f, half, width.toFloat(), footerTop))
            drawLabel(canvas, referenceLabel, 24f, 24f, paint)
            drawLabel(canvas, resultLabel, 24f, half + 24f, paint)
        } else {
            val half = width / 2f
            drawCenterCrop(canvas, reference, RectF(0f, 0f, half, footerTop))
            drawCenterCrop(canvas, result, RectF(half, 0f, width.toFloat(), footerTop))
            drawLabel(canvas, referenceLabel, 24f, 24f, paint)
            drawLabel(canvas, resultLabel, half + 24f, 24f, paint)
        }
        drawBranding(canvas, projectName, footerTop, height.toFloat())
        saveImage(context, projectName, "reference-result", bitmap).also { bitmap.recycle() }
    }

    suspend fun createTimelapse(
        context: Context,
        projectName: String,
        photos: List<Bitmap>,
        shotDurationMs: Long,
        onProgress: (Int) -> Unit,
    ): Uri {
        require(photos.isNotEmpty())
        val workDir = withContext(Dispatchers.IO) {
            File(context.cacheDir, "social-exports/${UUID.randomUUID()}").apply { mkdirs() }
        }
        val duration = shotDurationMs.coerceIn(100L, 3_000L)
        val frameFiles = try {
            withContext(Dispatchers.IO) {
                photos.mapIndexedNotNull { index, photo ->
                    runCatching {
                        val frame = createSocialFrame(photo, projectName)
                        File(workDir, "frame_${index.toString().padStart(4, '0')}.jpg").also { file ->
                            file.outputStream().buffered().use { output ->
                                check(frame.compress(Bitmap.CompressFormat.JPEG, 92, output))
                            }
                            frame.recycle()
                        }
                    }.getOrNull()
                }.also { require(it.isNotEmpty()) }
            }
        } catch (error: Throwable) {
            workDir.deleteRecursively()
            throw error
        }
        val output = File(workDir, "timelapse.mp4")
        try {
            exportImageSequence(context, frameFiles, duration, output, onProgress)
            return withContext(Dispatchers.IO) {
                publishVideo(context, projectName, output)
            }
        } finally {
            workDir.deleteRecursively()
        }
    }

    private suspend fun exportImageSequence(
        context: Context,
        frames: List<File>,
        durationMs: Long,
        output: File,
        onProgress: (Int) -> Unit,
    ) = suspendCancellableCoroutine { continuation ->
        val missingDuration = (1_000L - frames.size * durationMs).coerceAtLeast(0L)
        val items = frames.mapIndexed { index, file ->
            val mediaItem = MediaItem.Builder()
                .setUri(file.toUri())
                .setImageDurationMs(durationMs + if (index == frames.lastIndex) missingDuration else 0L)
                .build()
            EditedMediaItem.Builder(mediaItem).setFrameRate(30).build()
        }
        val sequence = EditedMediaItemSequence.Builder(setOf(C.TRACK_TYPE_VIDEO))
            .addItems(items)
            .build()
        val composition = Composition.Builder(sequence).build()
        val handler = Handler(Looper.getMainLooper())
        lateinit var transformer: Transformer
        val progress = object : Runnable {
            override fun run() {
                val holder = ProgressHolder()
                val state = transformer.getProgress(holder)
                if (state != Transformer.PROGRESS_STATE_NOT_STARTED) onProgress(holder.progress)
                if (continuation.isActive) handler.postDelayed(this, 300L)
            }
        }
        transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setUsePlatformDiagnostics(false)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    handler.removeCallbacks(progress)
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException,
                ) {
                    handler.removeCallbacks(progress)
                    if (continuation.isActive) continuation.resumeWithException(exportException)
                }
            })
            .build()
        continuation.invokeOnCancellation {
            handler.removeCallbacks(progress)
            transformer.cancel()
        }
        transformer.start(composition, output.absolutePath)
        handler.post(progress)
    }

    fun share(context: Context, uri: Uri, mimeType: String, packageName: String? = null) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = android.content.ClipData.newRawUri("TraceLens", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (packageName != null) setPackage(packageName)
        }
        runCatching { context.startActivity(intent) }.onFailure {
            val fallback = Intent.createChooser(intent.apply { setPackage(null) }, null)
            context.startActivity(fallback)
        }
    }

    private fun createSocialFrame(source: Bitmap, projectName: String): Bitmap {
        val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(DARK)
        drawCenterCrop(canvas, source, RectF(0f, 0f, 1080f, 1680f))
        drawBranding(canvas, projectName, 1680f, 1920f)
        return bitmap
    }

    private fun drawCenterCrop(canvas: Canvas, bitmap: Bitmap, destination: RectF) {
        val sourceRatio = bitmap.width.toFloat() / bitmap.height
        val destinationRatio = destination.width() / destination.height()
        val source = if (sourceRatio > destinationRatio) {
            val width = bitmap.height * destinationRatio
            val left = (bitmap.width - width) / 2f
            RectF(left, 0f, left + width, bitmap.height.toFloat())
        } else {
            val height = bitmap.width / destinationRatio
            val top = (bitmap.height - height) / 2f
            RectF(0f, top, bitmap.width.toFloat(), top + height)
        }
        canvas.drawBitmap(
            bitmap,
            Rect(source.left.toInt(), source.top.toInt(), source.right.toInt(), source.bottom.toInt()),
            destination,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
        )
    }

    private fun drawBranding(canvas: Canvas, projectName: String, top: Float, bottom: Float) {
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = DARK }
        canvas.drawRect(0f, top, canvas.width.toFloat(), bottom, background)
        val centerY = (top + bottom) / 2f
        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = PURPLE }
        canvas.drawRoundRect(RectF(48f, centerY - 48f, 144f, centerY + 48f), 20f, 20f, iconPaint)
        iconPaint.color = Color.WHITE
        canvas.drawCircle(96f, centerY, 27f, iconPaint)
        iconPaint.color = DARK
        canvas.drawCircle(96f, centerY, 14f, iconPaint)
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawText("TraceLens", 172f, centerY + 17f, brandPaint)
        val projectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE3D9FF.toInt()
            textSize = 42f
            textAlign = Paint.Align.RIGHT
        }
        val available = 430f
        val shortName = ellipsize(projectName.trim().ifBlank { "Project" }, projectPaint, available)
        canvas.drawText(shortName, canvas.width - 48f, centerY + 15f, projectPaint)
    }

    private fun drawLabel(canvas: Canvas, label: String, x: Float, y: Float, paint: Paint) {
        val width = paint.measureText(label) + 32f
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xB3000000.toInt() }
        canvas.drawRoundRect(RectF(x, y, x + width, y + 58f), 14f, 14f, background)
        canvas.drawText(label, x + 16f, y + 42f, paint)
    }

    private fun ellipsize(text: String, paint: Paint, width: Float): String {
        if (paint.measureText(text) <= width) return text
        val suffix = "…"
        val count = paint.breakText(text, true, width - paint.measureText(suffix), null)
        return text.take(count.coerceAtLeast(1)).trimEnd() + suffix
    }

    private fun saveImage(context: Context, projectName: String, kind: String, bitmap: Bitmap): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "TraceLens_${kind}_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/TraceLens/${safeFolder(projectName)}")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Cannot create gallery image")
        try {
            resolver.openOutputStream(uri)?.use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
                ?: error("Cannot open gallery image")
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    private fun publishVideo(context: Context, projectName: String, source: File): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "TraceLens_timelapse_${System.currentTimeMillis()}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/TraceLens/${safeFolder(projectName)}")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Cannot create gallery video")
        try {
            resolver.openOutputStream(uri)?.use { output -> source.inputStream().use { it.copyTo(output) } }
                ?: error("Cannot open gallery video")
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    private fun safeFolder(name: String): String = name.trim().ifBlank { "Project" }
        .replace(Regex("[^A-Za-z0-9ąćęłńóśźżĄĆĘŁŃÓŚŹŻ _-]"), "_")
        .take(60)
}
