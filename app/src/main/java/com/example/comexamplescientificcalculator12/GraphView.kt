package com.example.comexamplescientificcalculator12

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class GraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var functionExpression: String = "x^2"
        set(value) {
            field = value
            invalidate()
        }

    private val axisPaint = Paint().apply {
        color = 0xFF64748B.toInt()
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = 0xFF1E293B.toInt()
        strokeWidth = 1f
        isAntiAlias = true
    }

    private val curvePaint = Paint().apply {
        color = 0xFF38BDF8.toInt()
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = 0xFF94A3B8.toInt()
        textSize = 32f
        isAntiAlias = true
    }

    private val engine = CalculatorEngine(isRadians = true)
    private val curvePath = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val scale = 40f

        // Draw grid
        var x = cx % scale
        while (x < w) {
            canvas.drawLine(x, 0f, x, h, gridPaint)
            x += scale
        }
        var y = cy % scale
        while (y < h) {
            canvas.drawLine(0f, y, w, y, gridPaint)
            y += scale
        }

        // Draw axes
        canvas.drawLine(0f, cy, w, cy, axisPaint)
        canvas.drawLine(cx, 0f, cx, h, axisPaint)

        // Draw curve
        curvePath.reset()
        var first = true
        val step = 2f

        var px = 0f
        while (px < w) {
            val mathX = (px - cx) / scale
            try {
                val exprWithX = functionExpression.replace("x", "($mathX)")
                val mathY = engine.evaluate(exprWithX)
                if (!mathY.isNaN() && !mathY.isInfinite()) {
                    val py = cy - (mathY.toFloat() * scale)
                    if (first) {
                        curvePath.moveTo(px, py)
                        first = false
                    } else {
                        curvePath.lineTo(px, py)
                    }
                }
            } catch (_: Exception) {
                // Skip discontinuity
            }
            px += step
        }
        canvas.drawPath(curvePath, curvePaint)

        // Draw function label and properties info
        canvas.drawText("f(x) = $functionExpression", 30f, 60f, textPaint)
        canvas.drawText("Properties: Continuous on ℝ | Scale: 40px/unit", 30f, 110f, textPaint)
    }
}
