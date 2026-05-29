package com.example.mobileproject

import android.graphics.Color

enum class ShapeType(val displayName: String, val emoji: String) {
    CIRCLE("Круг", "●"),
    SQUARE("Квадрат", "■"),
    TRIANGLE("Треугольник", "▲"),
    STAR("Звезда", "★"),
    DIAMOND("Ромб", "◆"),
    HEART("Сердце", "♥")
}

object ShapeColors {
    val RED    = Color.parseColor("#E53935")
    val BLUE   = Color.parseColor("#1E88E5")
    val GREEN  = Color.parseColor("#43A047")
    val YELLOW = Color.parseColor("#FDD835")
    val PURPLE = Color.parseColor("#8E24AA")
    val ORANGE = Color.parseColor("#FB8C00")
    val ALL = listOf(RED, BLUE, GREEN, YELLOW, PURPLE, ORANGE)
}

data class Shape(
    val type: ShapeType,
    val color: Int,
    var x: Float,
    var y: Float,
    var dx: Float,
    var dy: Float,
    val size: Float
)

data class LevelConfig(
    val level: Int,
    val shapeCount: Int,
    val shapeTypes: List<ShapeType>,
    val speedMultiplier: Float,
    val displaySeconds: Int
)

object Levels {
    // Все доступные типы фигур по порядку добавления
    private val allTypes = ShapeType.entries.toList()

    // Уровни 1-3: фиксированные, простые
    // Уровень 1: 4 фигуры, 2 вида, 15 сек
    // Уровень 2: 5 фигур, 2 вида, 14 сек
    // Уровень 3: 6 фигур, 3 вида, 13 сек
    // Начиная с уровня 4: шаг +1 фигура, при 10 фигурах → +1 вид, откат до 7

    fun getConfig(level: Int): LevelConfig {
        return when {
            level <= 1 -> LevelConfig(1, 4, allTypes.take(2), 1.0f, 15)
            level == 2 -> LevelConfig(2, 5, allTypes.take(2), 1.1f, 14)
            level == 3 -> LevelConfig(3, 6, allTypes.take(3), 1.2f, 13)
            else -> {
                // Начиная с уровня 4: базовые 7 фигур, 3 вида
                // Каждый уровень +1 фигура; при достижении 10 → новый вид, откат до 7
                val stepsFrom4 = level - 4  // 0, 1, 2, 3, 4, 5, 6, ...

                // Каждые 3 шага (0→6→9) происходит сброс с добавлением вида
                // Цикл: 7, 8, 9, [10→сброс: новый вид, 7], 8, 9, [10→сброс], ...
                // За каждый полный цикл (3 шага) добавляется 1 вид
                val cycleLength = 3  // 7→8→9→(10=reset)
                val completedCycles = stepsFrom4 / cycleLength
                val posInCycle = stepsFrom4 % cycleLength

                val typeCount = (3 + completedCycles).coerceAtMost(allTypes.size)
                val shapeCount = 7 + posInCycle  // 7, 8, 9 внутри цикла

                val speed = 1.2f + stepsFrom4 * 0.1f
                val seconds = (13 - completedCycles * 2).coerceAtLeast(6)

                LevelConfig(level, shapeCount, allTypes.take(typeCount), speed, seconds)
            }
        }
    }
}

data class GameState(
    val level: Int = 1,
    val score: Int = 0,
    val wrongAnswers: Int = 0,
    val phase: GamePhase = GamePhase.SHOWING,
    val correctCounts: Map<ShapeType, Int> = emptyMap()
)

enum class GamePhase { SHOWING, ANSWERING, RESULT }
