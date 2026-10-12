package br.com.mnunesnails.financas.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/** Small vector illustrations drawn at any density, without bitmap assets. */
class IconView(context: Context, private val symbol: String, private val tint: Int) : View(context) {
    private val pen = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scale = minOf(width, height) / 24f
        canvas.save()
        canvas.translate((width - 24 * scale) / 2, (height - 24 * scale) / 2)
        canvas.scale(scale, scale)
        pen.color = tint
        pen.style = Paint.Style.STROKE
        pen.strokeWidth = 1.7f
        pen.strokeCap = Paint.Cap.ROUND
        pen.strokeJoin = Paint.Join.ROUND
        fun line(x: Float, y: Float, xx: Float, yy: Float) = canvas.drawLine(x, y, xx, yy, pen)
        fun path(vararg points: Float) {
            val path = Path().apply {
                moveTo(points[0], points[1])
                for (i in 2 until points.size step 2) lineTo(points[i], points[i + 1])
            }
            canvas.drawPath(path, pen)
        }
        when (symbol) {
            "plus" -> { line(12f, 4f, 12f, 20f); line(4f, 12f, 20f, 12f) }
            "close" -> { line(6f, 6f, 18f, 18f); line(18f, 6f, 6f, 18f) }
            "back" -> path(14f, 5f, 7f, 12f, 14f, 19f)
            "down" -> path(6f, 9f, 12f, 15f, 18f, 9f)
            "menu" -> { line(4f, 6f, 20f, 6f); line(4f, 12f, 20f, 12f); line(4f, 18f, 20f, 18f) }
            "list" -> for (y in listOf(6f, 12f, 18f)) { canvas.drawCircle(4f, y, .6f, pen); line(9f, y, 20f, y) }
            "search" -> { canvas.drawCircle(10.5f, 10.5f, 6.5f, pen); line(15.5f, 15.5f, 21f, 21f) }
            "home" -> { path(3f, 11f, 12f, 3f, 21f, 11f); path(5f, 10f, 5f, 21f, 10f, 21f, 10f, 15f, 14f, 15f, 14f, 21f, 19f, 21f, 19f, 10f) }
            "chart" -> { canvas.drawRoundRect(4f, 13f, 7f, 21f, 1f, 1f, pen); canvas.drawRoundRect(11f, 8f, 14f, 21f, 1f, 1f, pen); canvas.drawRoundRect(18f, 3f, 21f, 21f, 1f, 1f, pen) }
            "more" -> { pen.style = Paint.Style.FILL; for (x in listOf(5f, 12f, 19f)) canvas.drawCircle(x, 12f, 1.6f, pen) }
            "user" -> { canvas.drawCircle(12f, 7f, 3.5f, pen); canvas.drawRoundRect(5f, 14f, 19f, 21f, 3f, 3f, pen) }
            "calendar" -> { canvas.drawRoundRect(3f, 5f, 21f, 21f, 2f, 2f, pen); line(3f, 10f, 21f, 10f); line(8f, 3f, 8f, 7f); line(16f, 3f, 16f, 7f); line(8f, 15f, 12f, 15f) }
            "clock" -> { canvas.drawCircle(12f, 12f, 9f, pen); path(12f, 6f, 12f, 12f, 16f, 14f) }
            "card" -> { canvas.drawRoundRect(3f, 5f, 21f, 19f, 2f, 2f, pen); line(3f, 10f, 21f, 10f) }
            "note" -> { canvas.drawRoundRect(5f, 3f, 19f, 21f, 2f, 2f, pen); line(9f, 7f, 16f, 7f); line(9f, 11f, 16f, 11f); line(9f, 15f, 13f, 15f) }
            "tag" -> { path(3f, 12f, 12f, 3f, 21f, 3f, 21f, 12f, 12f, 21f, 3f, 12f); canvas.drawCircle(16.5f, 7.5f, 1f, pen) }
            "save" -> { path(4f, 3f, 18f, 3f, 21f, 6f, 21f, 21f, 3f, 21f, 3f, 3f, 4f, 3f); canvas.drawRect(7f, 3f, 16f, 9f, pen); canvas.drawRect(7f, 14f, 17f, 21f, pen) }
            "money" -> { canvas.drawCircle(12f, 12f, 9f, pen); pen.style = Paint.Style.FILL; pen.textSize = 15f; pen.textAlign = Paint.Align.CENTER; canvas.drawText("$", 12f, 17f, pen) }
            "info" -> { canvas.drawCircle(12f, 12f, 9f, pen); line(12f, 11f, 12f, 17f); canvas.drawCircle(12f, 7f, .5f, pen) }
            "nail" -> {
                canvas.save(); canvas.rotate(35f, 12f, 12f)
                canvas.drawRoundRect(8f, 2f, 16f, 22f, 4f, 4f, pen)
                line(8f, 13f, 16f, 13f)
                canvas.restore()
            }
            "flower" -> {
                pen.style = Paint.Style.FILL
                for ((angle, color) in listOf(-42f to 0xFFFF8DAA.toInt(), 42f to 0xFFFF8DAA.toInt(), 0f to 0xFFEE2864.toInt())) {
                    canvas.save(); canvas.rotate(angle, 12f, 21f); pen.color = color
                    canvas.drawPath(Path().apply { moveTo(12f, 22f); cubicTo(1f, 15f, 8f, 6f, 12f, 2f); cubicTo(17f, 7f, 23f, 16f, 12f, 22f) }, pen)
                    canvas.restore()
                }
            }
        }
        canvas.restore()
    }
}
