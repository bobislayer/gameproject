package com.example.mobileproject

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class ShapesView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val shapes = mutableListOf<Shape>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var running = false
    private var animationThread: Thread? = null
    private val targetPath = Path()

    fun startLevel(config: LevelConfig, shapeCounts: Map<ShapeType, Int>) {
        stopAnimation()
        shapes.clear()
        running = true
        post {
            if (width == 0 || height == 0) return@post
            // Build shapes list exactly matching the counts
            for ((type, count) in shapeCounts) {
                repeat(count) { shapes.add(createShapeOfType(type, config)) }
            }
            shapes.shuffle()
            startAnimation()
        }
    }

    fun stopAnimation() {
        running = false
        animationThread?.interrupt()
        animationThread = null
    }

    private fun createShapeOfType(type: ShapeType, config: LevelConfig): Shape {
        val size = resources.displayMetrics.density * (18f + Random.nextFloat() * 18f)
        val margin = size + 8f
        val x = Random.nextFloat() * (width - 2 * margin) + margin
        val y = Random.nextFloat() * (height - 2 * margin) + margin
        val baseSpeed = resources.displayMetrics.density * 1.8f * config.speedMultiplier
        val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
        return Shape(
            type  = type,
            color = ShapeColors.ALL.random(),
            x = x, y = y,
            dx = kotlin.math.cos(angle) * baseSpeed,
            dy = kotlin.math.sin(angle) * baseSpeed,
            size = size
        )
    }

    private fun startAnimation() {
        animationThread = Thread {
            while (running) {
                val start = System.currentTimeMillis()
                synchronized(shapes) { updatePositions() }
                postInvalidate()
                val sleep = 16L - (System.currentTimeMillis() - start)
                if (sleep > 0) try { Thread.sleep(sleep) } catch (_: InterruptedException) { break }
            }
        }.also { it.isDaemon = true; it.start() }
    }

    private fun updatePositions() {
        for (s in shapes) {
            s.x += s.dx; s.y += s.dy
            if (s.x - s.size < 0) { s.x = s.size; s.dx = -s.dx }
            if (s.x + s.size > width) { s.x = width - s.size; s.dx = -s.dx }
            if (s.y - s.size < 0) { s.y = s.size; s.dy = -s.dy }
            if (s.y + s.size > height) { s.y = height - s.size; s.dy = -s.dy }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(shapes) { for (s in shapes) drawShape(canvas, s) }
    }

    private fun drawShape(canvas: Canvas, shape: Shape) {
        shadowPaint.color = Color.argb(60, 0, 0, 0)
        shadowPaint.maskFilter = BlurMaskFilter(shape.size * 0.4f, BlurMaskFilter.Blur.NORMAL)
        drawShapeType(canvas, shape, shadowPaint, offsetX = shape.size * 0.15f, offsetY = shape.size * 0.2f)
        paint.color = shape.color
        paint.maskFilter = null
        drawShapeType(canvas, shape, paint)
        paint.color = Color.argb(80, 255, 255, 255)
        drawShapeType(canvas, shape, paint, scale = 0.55f, offsetX = -shape.size * 0.2f, offsetY = -shape.size * 0.2f)
    }

    private fun drawShapeType(canvas: Canvas, shape: Shape, p: Paint, scale: Float = 1f, offsetX: Float = 0f, offsetY: Float = 0f) {
        val s = shape.size * scale
        val cx = shape.x + offsetX
        val cy = shape.y + offsetY
        when (shape.type) {
            ShapeType.CIRCLE -> canvas.drawCircle(cx, cy, s, p)
            ShapeType.SQUARE -> canvas.drawRoundRect(cx-s, cy-s, cx+s, cy+s, s*0.2f, s*0.2f, p)
            ShapeType.TRIANGLE -> {
                targetPath.reset()
                targetPath.moveTo(cx, cy-s); targetPath.lineTo(cx+s, cy+s); targetPath.lineTo(cx-s, cy+s); targetPath.close()
                canvas.drawPath(targetPath, p)
            }
            ShapeType.STAR -> drawStar(canvas, cx, cy, s, p)
            ShapeType.DIAMOND -> {
                targetPath.reset()
                targetPath.moveTo(cx, cy-s); targetPath.lineTo(cx+s, cy); targetPath.lineTo(cx, cy+s); targetPath.lineTo(cx-s, cy); targetPath.close()
                canvas.drawPath(targetPath, p)
            }
            ShapeType.HEART -> drawHeart(canvas, cx, cy, s, p)
        }
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, r: Float, p: Paint) {
        val path = Path()
        for (i in 0 until 10) {
            val angle = (Math.PI / 5 * i - Math.PI / 2).toFloat()
            val radius = if (i % 2 == 0) r else r * 0.45f
            val x = cx + radius * kotlin.math.cos(angle)
            val y = cy + radius * kotlin.math.sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close(); canvas.drawPath(path, p)
    }

    private fun drawHeart(canvas: Canvas, cx: Float, cy: Float, r: Float, p: Paint) {
        val path = Path()
        val sc = r / 10f
        path.moveTo(cx, cy + 3*sc)
        path.cubicTo(cx, cy, cx-5*sc, cy-3*sc, cx-5*sc, cy-6*sc)
        path.cubicTo(cx-5*sc, cy-10*sc, cx, cy-10*sc, cx, cy-7*sc)
        path.cubicTo(cx, cy-10*sc, cx+5*sc, cy-10*sc, cx+5*sc, cy-6*sc)
        path.cubicTo(cx+5*sc, cy-3*sc, cx, cy, cx, cy+3*sc)
        path.close(); canvas.drawPath(path, p)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}
