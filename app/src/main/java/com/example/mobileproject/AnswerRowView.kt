package com.example.mobileproject

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.util.AttributeSet
import android.view.Gravity
import android.widget.*

class AnswerRowView @JvmOverloads constructor(
    context: Context,
    val shapeType: ShapeType,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val etAnswer: EditText

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(16, 12, 16, 12)

        val tvName = TextView(context).apply {
            text = shapeType.displayName
            textSize = 17f
            setTextColor(Color.WHITE)
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        etAnswer = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "0"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#80FFFFFF"))
            background = null
            setBackgroundColor(Color.parseColor("#33FFFFFF"))
            setPadding(24, 8, 24, 8)
            layoutParams = LayoutParams(120, LayoutParams.WRAP_CONTENT)
        }

        addView(tvName)
        addView(etAnswer)
    }

    fun getAnswer(): Int = etAnswer.text.toString().trim().toIntOrNull() ?: 0

    fun showError(correct: Int) {
        setBackgroundColor(Color.parseColor("#33E53935"))
        etAnswer.setText(correct.toString())
        etAnswer.setTextColor(Color.parseColor("#E53935"))
    }

    fun showCorrect() {
        setBackgroundColor(Color.parseColor("#3343A047"))
        etAnswer.setTextColor(Color.parseColor("#43A047"))
    }
}
