package com.example.mobileproject

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mobileproject.databinding.FragmentGameBinding

class GameFragment : Fragment() {
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private var gameState = GameState()
    private var timer: CountDownTimer? = null
    private val currentShapeCounts = mutableMapOf<ShapeType, Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startLevel()
        binding.btnSubmit.setOnClickListener { checkAnswers() }
        binding.btnReady.setOnClickListener { skipTimer() }
        binding.btnMenu.setOnClickListener {
            binding.shapesView.stopAnimation()
            timer?.cancel()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val focusedView = requireActivity().currentFocus ?: binding.root
        imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
        binding.root.clearFocus()
    }

    private fun startLevel() {
        hideKeyboard()
        val config = Levels.getConfig(gameState.level)
        gameState = gameState.copy(phase = GamePhase.SHOWING)
        updateHUD()
        binding.shapesView.visibility = View.VISIBLE
        binding.answerScroll.visibility = View.GONE
        binding.tvPhaseLabel.text = "Запоминай фигуры"
        binding.timerBar.visibility = View.VISIBLE
        binding.btnReady.visibility = View.VISIBLE

        currentShapeCounts.clear()
        val rng = kotlin.random.Random
        repeat(config.shapeCount) {
            val type = config.shapeTypes[rng.nextInt(config.shapeTypes.size)]
            currentShapeCounts[type] = (currentShapeCounts[type] ?: 0) + 1
        }

        binding.shapesView.startLevel(config, currentShapeCounts.toMap())
        startCountdown(config.displaySeconds)
    }

    private fun skipTimer() {
        timer?.cancel()
        binding.progressBar.progress = 0
        switchToAnswerPhase()
    }

    private fun startCountdown(seconds: Int) {
        binding.progressBar.max = seconds * 10
        binding.progressBar.progress = seconds * 10
        binding.tvTimer.text = seconds.toString()
        binding.tvTimer.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        timer?.cancel()
        timer = object : CountDownTimer(seconds * 1000L, 100L) {
            override fun onTick(millisUntilFinished: Long) {
                binding.progressBar.progress = (millisUntilFinished / 100).toInt()
                binding.tvTimer.text = (millisUntilFinished / 1000 + 1).toString()
                if (millisUntilFinished < 3000)
                    binding.tvTimer.setTextColor(
                        ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
                    )
            }
            override fun onFinish() {
                binding.progressBar.progress = 0
                switchToAnswerPhase()
            }
        }.start()
    }

    private fun switchToAnswerPhase() {
        binding.shapesView.stopAnimation()
        binding.shapesView.visibility = View.GONE
        binding.timerBar.visibility = View.GONE
        binding.btnReady.visibility = View.GONE
        binding.tvPhaseLabel.text = "Сколько было каждой фигуры?"
        val config = Levels.getConfig(gameState.level)
        binding.answerPanel.removeAllViews()
        for (shapeType in config.shapeTypes)
            binding.answerPanel.addView(AnswerRowView(requireContext(), shapeType))
        binding.answerScroll.visibility = View.VISIBLE
        binding.answerScroll.startAnimation(
            AnimationUtils.loadAnimation(requireContext(), android.R.anim.slide_in_left)
        )
    }

    private fun checkAnswers() {
        hideKeyboard()
        var allCorrect = true
        for (i in 0 until binding.answerPanel.childCount) {
            val row = binding.answerPanel.getChildAt(i) as? AnswerRowView ?: continue
            val correct = currentShapeCounts[row.shapeType] ?: 0
            if (row.getAnswer() != correct) { allCorrect = false; row.showError(correct) }
            else row.showCorrect()
        }
        if (allCorrect) {
            val points = gameState.level * 100
            gameState = gameState.copy(score = gameState.score + points, level = gameState.level + 1)
            Toast.makeText(requireContext(), "Правильно! +$points очков", Toast.LENGTH_SHORT).show()
            binding.root.postDelayed({ startLevel() }, 1500)
        } else {
            val newWrong = gameState.wrongAnswers + 1
            gameState = gameState.copy(wrongAnswers = newWrong)
            updateHUD()
            if (newWrong >= 3) binding.root.postDelayed({ showGameOver() }, 1200)
            else {
                Toast.makeText(
                    requireContext(),
                    "Ошибка! Осталось попыток: ${3 - newWrong}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.root.postDelayed({ startLevel() }, 1800)
            }
        }
    }

    private fun showGameOver() {
        val prefs = requireContext().getSharedPreferences("AttentionTrainer", 0)
        val best = prefs.getInt("best_score", 0)
        if (gameState.score > best) prefs.edit().putInt("best_score", gameState.score).apply()
        (activity as? MainActivity)?.navigateTo(
            GameOverFragment.newInstance(gameState.score, gameState.level - 1)
        )
    }

    private fun updateHUD() {
        binding.tvScore.text = "Очки: ${gameState.score}"
        binding.tvLevel.text = "Уровень ${gameState.level}"
        binding.tvLives.text = "x${3 - gameState.wrongAnswers}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        if (_binding != null) binding.shapesView.stopAnimation()
        _binding = null
    }
}
