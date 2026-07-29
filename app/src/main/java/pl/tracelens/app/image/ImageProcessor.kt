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
        val bitmap = downscale(source, 2048)
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

        for (index in input.indices) {
            val color = input[index]
            var r = Color.red(color)
            var g = Color.green(color)
            var b = Color.blue(color)
            var a = Color.alpha(color)

            if (removeColor != null) {
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
            output[index] = Color.argb(a, r, g, b)
        }

        val finalPixels = if (filters.contour) sobel(output, width, height) else output
        return Bitmap.createBitmap(finalPixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun contrast(value: Int, amount: Float): Int =
        (((value - 128) * amount) + 128).toInt().coerceIn(0, 255)

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
