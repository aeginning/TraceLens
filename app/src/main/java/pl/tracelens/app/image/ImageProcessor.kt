package pl.tracelens.app.image

import android.graphics.Bitmap
import android.graphics.Color
import pl.tracelens.app.data.ImageFilters
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object ImageProcessor {
    fun process(source: Bitmap, filters: ImageFilters): Bitmap {
        val bitmap = downscale(crop(source, filters), 2048)
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
            detectConnectedBackground(input, width, height, tolerance.coerceAtLeast(24f))
        } else null

        for (index in input.indices) {
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

        val finalPixels = if (filters.contour) sobel(output, width, height) else output
        applyEraser(finalPixels, width, height, filters)
        return Bitmap.createBitmap(finalPixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun quantize(value: Int, levels: Int): Int {
        val step = 255f / (levels.coerceIn(2, 16) - 1)
        return (kotlin.math.round(value / step) * step).toInt().coerceIn(0, 255)
    }

    private fun detectConnectedBackground(
        pixels: IntArray,
        width: Int,
        height: Int,
        tolerance: Float,
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

    private fun sobel(pixels: IntArray, width: Int, height: Int): IntArray {
        val result = IntArray(pixels.size)
        fun luminance(x: Int, y: Int): Int = Color.red(pixels[y * width + x])
        for (y in 1 until height - 1) {
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
