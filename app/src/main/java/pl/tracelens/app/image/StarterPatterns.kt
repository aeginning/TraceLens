package pl.tracelens.app.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class StarterPattern { Star, Heart, Flower, Butterfly }

object StarterPatterns {
    fun create(pattern: StarterPattern, size: Int = 900): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(80, 48, 130)
            style = Paint.Style.STROKE
            strokeWidth = size * 0.025f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        when (pattern) {
            StarterPattern.Star -> drawStar(canvas, paint, size)
            StarterPattern.Heart -> drawHeart(canvas, paint, size)
            StarterPattern.Flower -> drawFlower(canvas, paint, size)
            StarterPattern.Butterfly -> drawButterfly(canvas, paint, size)
        }
        return bitmap
    }

    private fun drawStar(canvas: Canvas, paint: Paint, size: Int) {
        val path = Path()
        val center = size / 2f
        for (point in 0 until 10) {
            val radius = if (point % 2 == 0) size * 0.38f else size * 0.16f
            val angle = -PI / 2 + point * PI / 5
            val x = center + cos(angle).toFloat() * radius
            val y = center + sin(angle).toFloat() * radius
            if (point == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawHeart(canvas: Canvas, paint: Paint, size: Int) {
        val path = Path().apply {
            moveTo(size * 0.5f, size * 0.78f)
            cubicTo(size * 0.38f, size * 0.66f, size * 0.16f, size * 0.52f, size * 0.18f, size * 0.32f)
            cubicTo(size * 0.2f, size * 0.12f, size * 0.43f, size * 0.14f, size * 0.5f, size * 0.29f)
            cubicTo(size * 0.57f, size * 0.14f, size * 0.8f, size * 0.12f, size * 0.82f, size * 0.32f)
            cubicTo(size * 0.84f, size * 0.52f, size * 0.62f, size * 0.66f, size * 0.5f, size * 0.78f)
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawFlower(canvas: Canvas, paint: Paint, size: Int) {
        val center = size / 2f
        for (petal in 0 until 8) {
            val angle = petal * PI / 4
            val x = center + cos(angle).toFloat() * size * 0.22f
            val y = center + sin(angle).toFloat() * size * 0.22f
            canvas.drawOval(
                x - size * 0.12f, y - size * 0.18f,
                x + size * 0.12f, y + size * 0.18f, paint,
            )
        }
        canvas.drawCircle(center, center, size * 0.13f, paint)
        canvas.drawLine(center, size * 0.63f, center, size * 0.88f, paint)
        canvas.drawOval(size * 0.5f, size * 0.72f, size * 0.72f, size * 0.84f, paint)
    }

    private fun drawButterfly(canvas: Canvas, paint: Paint, size: Int) {
        val path = Path().apply {
            moveTo(size * 0.48f, size * 0.48f)
            cubicTo(size * 0.34f, size * 0.12f, size * 0.08f, size * 0.18f, size * 0.2f, size * 0.48f)
            cubicTo(size * 0.05f, size * 0.68f, size * 0.3f, size * 0.84f, size * 0.48f, size * 0.58f)
            moveTo(size * 0.52f, size * 0.48f)
            cubicTo(size * 0.66f, size * 0.12f, size * 0.92f, size * 0.18f, size * 0.8f, size * 0.48f)
            cubicTo(size * 0.95f, size * 0.68f, size * 0.7f, size * 0.84f, size * 0.52f, size * 0.58f)
        }
        canvas.drawPath(path, paint)
        canvas.drawOval(size * 0.47f, size * 0.35f, size * 0.53f, size * 0.72f, paint)
        canvas.drawLine(size * 0.49f, size * 0.36f, size * 0.4f, size * 0.22f, paint)
        canvas.drawLine(size * 0.51f, size * 0.36f, size * 0.6f, size * 0.22f, paint)
    }
}
