package pl.tracelens.app.image

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import pl.tracelens.app.data.ImageFilters
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object ImageProcessor {
    fun process(
        source: Bitmap,
        filters: ImageFilters,
        shouldCancel: () -> Boolean = { false },
    ): Bitmap {
        val bitmap = downscale(crop(rotate(source, filters.rotationDegrees), filters), 2048)
        val width = bitmap.width
        val height = bitmap.height
        val input = IntArray(width * height)
        bitmap.getPixels(input, 0, width, 0, 0, width, height)
        val output = IntArray(input.size)

        val removeColor = filters.removeColor
        val rr = removeColor?.let(Color::red) ?: 0
        val rg = removeColor?.let(Color::green) ?: 0
        val rb = removeColor?.let(Color::blue) ?: 0
        val tolerance = filters.colorTolerance * 441.7f
        val automaticBackground = if (filters.autoRemoveBackground) {
            detectConnectedBackground(
                input,
                width,
                height,
                tolerance.coerceAtLeast(24f),
                shouldCancel,
            )
        } else null

        for (index in input.indices) {
            if (index % width == 0 && shouldCancel()) throw java.util.concurrent.CancellationException()
            val color = input[index]
            var r = Color.red(color)
            var g = Color.green(color)
            var b = Color.blue(color)
            var a = Color.alpha(color)

            if (automaticBackground?.get(index) == true) {
                a = 0
            } else if (removeColor != null) {
                val distance = sqrt(
                    ((r - rr) * (r - rr) + (g - rg) * (g - rg) + (b - rb) * (b - rb)).toFloat()
                )
                if (distance <= tolerance) a = 0
            }

            if (filters.grayscale || filters.contour) {
                val gray = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
                r = gray; g = gray; b = gray
            }

            if (filters.contrast != 1f) {
                r = contrast(r, filters.contrast)
                g = contrast(g, filters.contrast)
                b = contrast(b, filters.contrast)
            }

            if (filters.invert) {
                r = 255 - r; g = 255 - g; b = 255 - b
            }
            if (filters.simplifyLevels >= 2) {
                r = quantize(r, filters.simplifyLevels)
                g = quantize(g, filters.simplifyLevels)
                b = quantize(b, filters.simplifyLevels)
            }
            output[index] = Color.argb(a, r, g, b)
        }

        val finalPixels = if (filters.contour) sobel(output, width, height, shouldCancel) else output
        if (filters.artifactCleanup > 0) {
            removeSmallIslands(finalPixels, width, height, filters.artifactCleanup, shouldCancel)
        }
        applyEraser(finalPixels, width, height, filters)
        return Bitmap.createBitmap(finalPixels, width, height, Bitmap.Config.ARGB_8888)
    }

    fun previewSource(source: Bitmap, maxDimension: Int = 1024): Bitmap =
        downscale(source, maxDimension)

    private fun quantize(value: Int, levels: Int): Int {
        val step = 255f / (levels.coerceIn(2, 16) - 1)
        return (kotlin.math.round(value / step) * step).toInt().coerceIn(0, 255)
    }

    /**
     * Estimates a small skew angle from strong luminance edges. The returned value is the
     * corrective rotation, limited to ±15 degrees so a photograph cannot be turned sideways.
     */
    fun detectStraighteningAngle(source: Bitmap): Float {
        val bitmap = downscale(source, 640)
        val width = bitmap.width
        val height = bitmap.height
        if (width < 16 || height < 16) return 0f
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        var bestAngle = 0
        var bestScore = Double.NEGATIVE_INFINITY
        for (angle in -15..15) {
            val radians = Math.toRadians(angle.toDouble())
            val cos = kotlin.math.cos(radians)
            val sin = kotlin.math.sin(radians)
            val bins = DoubleArray(height + width)
            for (y in 2 until height - 2 step 2) {
                for (x in 2 until width - 2 step 2) {
                    fun lum(px: Int) = 0.299 * Color.red(px) + 0.587 * Color.green(px) + 0.114 * Color.blue(px)
                    val gx = lum(pixels[y * width + x + 1]) - lum(pixels[y * width + x - 1])
                    val gy = lum(pixels[(y + 1) * width + x]) - lum(pixels[(y - 1) * width + x])
                    val strength = kotlin.math.abs(gx) + kotlin.math.abs(gy)
                    if (strength > 80) {
                        val projected = (y * cos - x * sin + width / 2).toInt()
                        if (projected in bins.indices) bins[projected] += strength
                    }
                }
            }
            val mean = bins.average()
            val score = bins.sumOf { (it - mean) * (it - mean) }
            if (score > bestScore) {
                bestScore = score
                bestAngle = angle
            }
        }
        return (-bestAngle).toFloat()
    }

    private fun rotate(source: Bitmap, degrees: Float): Bitmap {
        if (kotlin.math.abs(degrees) < 0.05f) return source
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun removeSmallIslands(
        pixels: IntArray,
        width: Int,
        height: Int,
        strength: Int,
        shouldCancel: () -> Boolean,
    ) {
        val visited = BooleanArray(pixels.size)
        val queue = IntArray(pixels.size)
        val component = IntArray(pixels.size)
        val limit = (1 + pixels.size * strength / 2_000_000).coerceAtMost(2_000)
        for (start in pixels.indices) {
            if (start % width == 0 && shouldCancel()) throw java.util.concurrent.CancellationException()
            if (visited[start] || Color.alpha(pixels[start]) < 24) continue
            var head = 0
            var tail = 0
            var count = 0
            visited[start] = true
            queue[tail++] = start
            while (head < tail) {
                val index = queue[head++]
                component[count++] = index
                if (count > limit) break
                val x = index % width
                val neighbors = intArrayOf(
                    if (x > 0) index - 1 else -1,
                    if (x + 1 < width) index + 1 else -1,
                    if (index >= width) index - width else -1,
                    if (index + width < pixels.size) index + width else -1,
                )
                neighbors.forEach { next ->
                    if (next >= 0 && !visited[next] && Color.alpha(pixels[next]) >= 24) {
                        visited[next] = true
                        queue[tail++] = next
                    }
                }
            }
            if (count <= limit && head == tail) {
                for (i in 0 until count) pixels[component[i]] = pixels[component[i]] and 0x00FFFFFF
            }
        }
    }

    private fun detectConnectedBackground(
        pixels: IntArray,
        width: Int,
        height: Int,
        tolerance: Float,
        shouldCancel: () -> Boolean,
    ): BooleanArray {
        val result = BooleanArray(pixels.size)
        if (pixels.isEmpty()) return result
        val samples = intArrayOf(
            pixels.first(),
            pixels[width - 1],
            pixels[(height - 1) * width],
            pixels.last(),
        )
        val targetR = samples.sumOf { Color.red(it) } / samples.size
        val targetG = samples.sumOf { Color.green(it) } / samples.size
        val targetB = samples.sumOf { Color.blue(it) } / samples.size
        fun matches(index: Int): Boolean {
            val color = pixels[index]
            val dr = Color.red(color) - targetR
            val dg = Color.green(color) - targetG
            val db = Color.blue(color) - targetB
            return sqrt((dr * dr + dg * dg + db * db).toFloat()) <= tolerance
        }
        val queue = IntArray(pixels.size)
        var head = 0
        var tail = 0
        fun offer(index: Int) {
            if (!result[index] && matches(index)) {
                result[index] = true
                queue[tail++] = index
            }
        }
        for (x in 0 until width) {
            offer(x)
            offer((height - 1) * width + x)
        }
        for (y in 0 until height) {
            offer(y * width)
            offer(y * width + width - 1)
        }
        while (head < tail) {
            if (head % width == 0 && shouldCancel()) throw java.util.concurrent.CancellationException()
            val index = queue[head++]
            val x = index % width
            val y = index / width
            if (x > 0) offer(index - 1)
            if (x + 1 < width) offer(index + 1)
            if (y > 0) offer(index - width)
            if (y + 1 < height) offer(index + width)
        }
        return result
    }

    private fun applyEraser(
        pixels: IntArray,
        width: Int,
        height: Int,
        filters: ImageFilters,
    ) {
        val shortest = min(width, height).toFloat()
        filters.eraserPoints.forEach { point ->
            val centerX = point.x * width
            val centerY = point.y * height
            val radius = point.radius * shortest
            val left = (centerX - radius).toInt().coerceAtLeast(0)
            val right = (centerX + radius).toInt().coerceAtMost(width - 1)
            val top = (centerY - radius).toInt().coerceAtLeast(0)
            val bottom = (centerY + radius).toInt().coerceAtMost(height - 1)
            val radiusSquared = radius * radius
            for (y in top..bottom) {
                for (x in left..right) {
                    val dx = x - centerX
                    val dy = y - centerY
                    if (dx * dx + dy * dy <= radiusSquared) {
                        pixels[y * width + x] = pixels[y * width + x] and 0x00FFFFFF
                    }
                }
            }
        }
    }

    private fun contrast(value: Int, amount: Float): Int =
        (((value - 128) * amount) + 128).toInt().coerceIn(0, 255)

    fun crop(source: Bitmap, filters: ImageFilters): Bitmap {
        val left = (filters.cropLeft.coerceIn(0f, 0.99f) * source.width).toInt()
        val top = (filters.cropTop.coerceIn(0f, 0.99f) * source.height).toInt()
        val right = (filters.cropRight.coerceIn(filters.cropLeft + 0.01f, 1f) * source.width)
            .toInt().coerceAtLeast(left + 1)
        val bottom = (filters.cropBottom.coerceIn(filters.cropTop + 0.01f, 1f) * source.height)
            .toInt().coerceAtLeast(top + 1)
        return Bitmap.createBitmap(
            source,
            left,
            top,
            (right - left).coerceAtMost(source.width - left),
            (bottom - top).coerceAtMost(source.height - top),
        )
    }

    private fun downscale(source: Bitmap, maxDimension: Int): Bitmap {
        val largest = max(source.width, source.height)
        if (largest <= maxDimension) return source.copy(Bitmap.Config.ARGB_8888, false)
        val ratio = maxDimension.toFloat() / largest
        return Bitmap.createScaledBitmap(
            source,
            (source.width * ratio).toInt(),
            (source.height * ratio).toInt(),
            true,
        )
    }

    private fun sobel(
        pixels: IntArray,
        width: Int,
        height: Int,
        shouldCancel: () -> Boolean,
    ): IntArray {
        val result = IntArray(pixels.size)
        fun luminance(x: Int, y: Int): Int = Color.red(pixels[y * width + x])
        for (y in 1 until height - 1) {
            if (shouldCancel()) throw java.util.concurrent.CancellationException()
            for (x in 1 until width - 1) {
                val gx = -luminance(x - 1, y - 1) + luminance(x + 1, y - 1) -
                    2 * luminance(x - 1, y) + 2 * luminance(x + 1, y) -
                    luminance(x - 1, y + 1) + luminance(x + 1, y + 1)
                val gy = -luminance(x - 1, y - 1) - 2 * luminance(x, y - 1) -
                    luminance(x + 1, y - 1) + luminance(x - 1, y + 1) +
                    2 * luminance(x, y + 1) + luminance(x + 1, y + 1)
                val edge = min(255, sqrt((gx * gx + gy * gy).toFloat()).toInt())
                val value = 255 - edge
                val alpha = Color.alpha(pixels[y * width + x])
                result[y * width + x] = Color.argb(alpha, value, value, value)
            }
        }
        return result
    }
}
