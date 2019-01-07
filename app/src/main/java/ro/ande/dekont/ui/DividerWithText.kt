package ro.ande.dekont.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import ro.ande.dekont.R

/**
 * A divider with text attached.
 * The text floats in the center of the line, and can have padding on its sides.
 */
class DividerWithText(context: Context, attrs: AttributeSet) : TextView(context, attrs) {
    private val paint = Paint()
    private val textBounds = Rect()
    private var textPadding: Float = 1f
    private var dividerWidth: Float = 1f

    init {
        gravity = Gravity.CENTER

        // Get attributes
        context.theme.obtainStyledAttributes(attrs, R.styleable.DividerWithText, 0, 0).apply {
            try {
                textPadding = getDimension(R.styleable.DividerWithText_textPadding, textPadding)
                dividerWidth = getDimension(R.styleable.DividerWithText_dividerWidth, dividerWidth)
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.apply {
            strokeWidth = dividerWidth
            color = getPaint().color
        }

        // Get the text's bounds
        getPaint().getTextBounds(text.toString(), 0, text.length, textBounds)

        // The y position of the lines
        val middleY = (height + textBounds.bottom + dividerWidth) / 2f

        // Draw left line
        canvas.drawLine(0f, middleY, (width - textBounds.right)/2 - textPadding, middleY, paint)
        // Draw right line
        canvas.drawLine(textPadding + (width + textBounds.right)/2, middleY, width.toFloat(), middleY, paint)
    }
}