package com.muthupandi.isai.stats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.muthupandi.isai.R

class StatsBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }

    private var data: List<Float> = emptyList()
    private var labels: List<String> = emptyList()
    private var maxDataValue: Float = 0f
    
    // Bar rounding radius
    private val cornerRadius = 12f

    fun setData(values: List<Float>, labels: List<String>) {
        this.data = values
        this.labels = labels
        this.maxDataValue = values.maxOrNull() ?: 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        // Resolve colors
        val primaryColor = context.getColor(android.R.color.system_accent1_500) // Fallback if no attr
        // Ideally we would resolve ?attr/colorPrimary and ?attr/colorOnSurfaceVariant
        // We'll use some default Material colors for simplicity if resolving fails.

        barPaint.color = 0xFF6200EE.toInt() // Placeholder primary
        textPaint.color = 0xFF888888.toInt() // Placeholder onSurfaceVariant

        val width = width.toFloat()
        val height = height.toFloat()

        val paddingBottom = 60f
        val availableHeight = height - paddingBottom
        val barWidth = width / (data.size * 2f)
        val spacing = barWidth

        for (i in data.indices) {
            val value = data[i]
            val barHeight = if (maxDataValue > 0) (value / maxDataValue) * availableHeight else 0f
            
            val left = i * (barWidth + spacing) + spacing / 2f
            val right = left + barWidth
            val top = availableHeight - barHeight
            val bottom = availableHeight
            
            val rect = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint)

            // Draw label
            if (i < labels.size) {
                val label = labels[i]
                val x = left + barWidth / 2f
                val y = height - 10f
                canvas.drawText(label, x, y, textPaint)
            }
        }
    }
}
