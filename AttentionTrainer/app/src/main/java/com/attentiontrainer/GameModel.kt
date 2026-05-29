package com.example.mobileproject

import android.graphics.Color

// ─── Типы фигур ───────────────────────────────────────────────────────────────

enum class ShapeType(val displayName: String, val emoji: String) {
    CIRCLE("Круг", "●"),
    SQUARE("Квадрат", "■"),
    TRIANGLE("Треугольник", "▲"),
    STAR("Звезда", "★"),
    DIAMOND("Ромб", "◆"),
    HEART("Сердце", "♥")
}

// ─── Цвета ────────────────────────────────────────────────────────────────────

object ShapeColors {
    val RED    = Color.parseColor("#E53935")
    val BLUE   = Color.parseColor("#1E88E5")
    val GREEN  = Color.parseColor("#43A047")
    val YELLOW = Color.parseColor("#FDD835")
    val PURPLE = Color.parseColor("#8E24AA")
    val ORANGE = Color.parseColor("#FB8C00")

    val ALL = listOf(RED, BLUE, GREEN, YELLOW, PURPLE, ORANGE)
}

// ─── Одна фигура на поле ──────────────────────────────────────────────────────

data class Shape(
    val type: ShapeType,
    val color: Int,
    var x: Float,
    var y: Float,
    var dx: Float,     // скорость по X
    var dy: Float,     // скорость по Y
    val size: Float    // радиус / полуразмер
)

// ─── Конфигурация уровня ──────────────────────────────────────────────────────

data class LevelConfig(
    val level: Int,
    val shapeCount: Int,          // сколько фигур одновременно на экране
    val shapeTypes: List<ShapeType>,
    val speedMultiplier: Float,   // множитель скорости
    val displaySeconds: Int       // сколько секунд показывать фигуры
)

object Levels {
    val configs = listOf(
        LevelConfig(
            level = 1,
            shapeCount = 5,
            shapeTypes = listOf(ShapeType.CIRCLE, ShapeType.SQUARE),
            speedMultiplier = 1.0f,
            displaySeconds = 8
        ),
        LevelConfig(
            level = 2,
            shapeCount = 8,
            shapeTypes = listOf(ShapeType.CIRCLE, ShapeType.SQUARE, ShapeType.TRIANGLE),
            speedMultiplier = 1.5f,
            displaySeconds = 7
        ),
        LevelConfig(
            level = 3,
            shapeCount = 12,
            shapeTypes = listOf(ShapeType.CIRCLE, ShapeType.SQUARE, ShapeType.TRIANGLE, ShapeType.STAR),
            speedMultiplier = 2.0f,
            displaySeconds = 6
        ),
        LevelConfig(
            level = 4,
            shapeCount = 15,
            shapeTypes = listOf(ShapeType.CIRCLE, ShapeType.SQUARE, ShapeType.TRIANGLE, ShapeType.STAR, ShapeType.DIAMOND),
            speedMultiplier = 2.5f,
            displaySeconds = 5
        ),
        LevelConfig(
            level = 5,
            shapeCount = 18,
            shapeTypes = ShapeType.entries,
            speedMultiplier = 3.0f,
            displaySeconds = 5
        )
    )

    fun getConfig(level: Int): LevelConfig = configs.getOrElse(level - 1) { configs.last() }
}

// ─── Состояние игры ───────────────────────────────────────────────────────────

data class GameState(
    val level: Int = 1,
    val score: Int = 0,
    val wrongAnswers: Int = 0,           // неверных ответов (макс. 3)
    val phase: GamePhase = GamePhase.SHOWING,
    val correctCounts: Map<ShapeType, Int> = emptyMap()
)

enum class GamePhase {
    SHOWING,   // фигуры движутся по экрану
    ANSWERING, // поле ввода ответов
    RESULT     // результат раунда
}
