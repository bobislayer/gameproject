package com.example.mobileproject

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min
import kotlin.random.Random

class ShapesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val shapes = mutableListOf<Shape>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var running = false
    private var animationThread: Thread? = null
    private val targetPath = Path()

    // ─── Инициализация ────────────────────────────────────────────────────────

    fun startLevel(config: LevelConfig) {
        stopAnimation()
        shapes.clear()
        running = true

        // Ждём, пока View получит размеры
        post {
            if (width == 0 || height == 0) return@post
            repeat(config.shapeCount) {
                shapes.add(createRandomShape(config))
            }
            startAnimation(config.speedMultiplier)
        }
    }

    fun stopAnimation() {
        running = false
        animationThread?.interrupt()
        animationThread = null
    }

    // ─── Создание фигуры ──────────────────────────────────────────────────────

    private fun createRandomShape(config: LevelConfig): Shape {
        val size = resources.displayMetrics.density * Random.nextFloat().let { 18f + it * 18f }
        val margin = size + 8f
        val x = Random.nextFloat() * (width - 2 * margin) + margin
        val y = Random.nextFloat() * (height - 2 * margin) + margin
        val baseSpeed = resources.displayMetrics.density * 1.8f * config.speedMultiplier
        val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
        return Shape(
            type  = config.shapeTypes.random(),
            color = ShapeColors.ALL.random(),
            x = x, y = y,
            dx = kotlin.math.cos(angle) * baseSpeed,
            dy = kotlin.math.sin(angle) * baseSpeed,
            size = size
        )
    }

    // ─── Цикл анимации ────────────────────────────────────────────────────────

    private fun startAnimation(speedMultiplier: Float) {
        animationThread = Thread {
            val frameMs = 16L
            while (running) {
                val startTime = System.currentTimeMillis()
                synchronized(shapes) { updatePositions() }
                postInvalidate()
                val elapsed = System.currentTimeMillis() - startTime
                val sleep = frameMs - elapsed
                if (sleep > 0) {
                    try { Thread.sleep(sleep) } catch (_: InterruptedException) { break }
                }
            }
        }.also { it.isDaemon = true; it.start() }
    }

    private fun updatePositions() {
        for (shape in shapes) {
            shape.x += shape.dx
            shape.y += shape.dy
            val margin = shape.size
            if (shape.x - margin < 0) { shape.x = margin; shape.dx = -shape.dx }
            if (shape.x + margin > width) { shape.x = width - margin; shape.dx = -shape.dx }
            if (shape.y - margin < 0) { shape.y = margin; shape.dy = -shape.dy }
            if (shape.y + margin > height) { shape.y = height - margin; shape.dy = -shape.dy }
        }
    }

    // ─── Отрисовка ────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(shapes) {
            for (shape in shapes) drawShape(canvas, shape)
        }
    }

    private fun drawShape(canvas: Canvas, shape: Shape) {
        // Тень
        shadowPaint.color = Color.argb(60, 0, 0, 0)
        shadowPaint.maskFilter = BlurMaskFilter(shape.size * 0.4f, BlurMaskFilter.Blur.NORMAL)
        drawShapeType(canvas, shape, shadowPaint, offsetX = shape.size * 0.15f, offsetY = shape.size * 0.2f)

        // Основная фигура
        paint.color = shape.color
        paint.maskFilter = null
        drawShapeType(canvas, shape, paint)

        // Блик
        paint.color = Color.argb(80, 255, 255, 255)
        drawShapeType(canvas, shape, paint, scale = 0.55f,
            offsetX = -shape.size * 0.2f, offsetY = -shape.size * 0.2f)
    }

    private fun drawShapeType(
        canvas: Canvas, shape: Shape, p: Paint,
        scale: Float = 1f, offsetX: Float = 0f, offsetY: Float = 0f
    ) {
        val s = shape.size * scale
        val cx = shape.x + offsetX
        val cy = shape.y + offsetY

        when (shape.type) {
            ShapeType.CIRCLE -> canvas.drawCircle(cx, cy, s, p)

            ShapeType.SQUARE -> canvas.drawRoundRect(
                cx - s, cy - s, cx + s, cy + s, s * 0.2f, s * 0.2f, p
            )

            ShapeType.TRIANGLE -> {
                targetPath.reset()
                targetPath.moveTo(cx, cy - s)
                targetPath.lineTo(cx + s, cy + s)
                targetPath.lineTo(cx - s, cy + s)
                targetPath.close()
                canvas.drawPath(targetPath, p)
            }

            ShapeType.STAR -> drawStar(canvas, cx, cy, s, p)

            ShapeType.DIAMOND -> {
                targetPath.reset()
                targetPath.moveTo(cx, cy - s)
                targetPath.lineTo(cx + s, cy)
                targetPath.lineTo(cx, cy + s)
                targetPath.lineTo(cx - s, cy)
                targetPath.close()
                canvas.drawPath(targetPath, p)
            }

            ShapeType.HEART -> drawHeart(canvas, cx, cy, s, p)
        }
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, r: Float, p: Paint) {
        val innerR = r * 0.45f
        val path = Path()
        for (i in 0 until 10) {
            val angle = (Math.PI / 5 * i - Math.PI / 2).toFloat()
            val radius = if (i % 2 == 0) r else innerR
            val x = cx + radius * kotlin.math.cos(angle)
            val y = cy + radius * kotlin.math.sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, p)
    }

    private fun drawHeart(canvas: Canvas, cx: Float, cy: Float, r: Float, p: Paint) {
        val path = Path()
        val scale = r / 10f
        path.moveTo(cx, cy + 3 * scale)
        path.cubicTo(cx, cy, cx - 5 * scale, cy - 3 * scale, cx - 5 * scale, cy - 6 * scale)
        path.cubicTo(cx - 5 * scale, cy - 10 * scale, cx, cy - 10 * scale, cx, cy - 7 * scale)
        path.cubicTo(cx, cy - 10 * scale, cx + 5 * scale, cy - 10 * scale, cx + 5 * scale, cy - 6 * scale)
        path.cubicTo(cx + 5 * scale, cy - 3 * scale, cx, cy, cx, cy + 3 * scale)
        path.close()
        canvas.drawPath(path, p)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}
