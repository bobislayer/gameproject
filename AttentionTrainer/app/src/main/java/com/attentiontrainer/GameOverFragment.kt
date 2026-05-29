package com.example.mobileproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.mobileproject.databinding.FragmentGameOverBinding

class GameOverFragment : Fragment() {

    private var _binding: FragmentGameOverBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_SCORE = "score"
        private const val ARG_LEVEL = "level"

        fun newInstance(score: Int, level: Int) = GameOverFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_SCORE, score)
                putInt(ARG_LEVEL, level)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameOverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val score = arguments?.getInt(ARG_SCORE) ?: 0
        val level = arguments?.getInt(ARG_LEVEL) ?: 1

        binding.tvScore.text = "$score"
        binding.tvLevel.text = "Достигнут уровень $level"

        val prefs = requireContext().getSharedPreferences("AttentionTrainer", 0)
        val best = prefs.getInt("best_score", 0)
        if (score >= best && score > 0) {
            binding.tvBest.text = "🏆 Новый рекорд!"
            binding.tvBest.visibility = View.VISIBLE
        }

        val bounce = AnimationUtils.loadAnimation(requireContext(), android.R.anim.bounce_interpolator)
        binding.tvScore.startAnimation(bounce)

        binding.btnRestart.setOnClickListener {
            // Очищаем весь backstack и начинаем заново
            repeat(requireActivity().supportFragmentManager.backStackEntryCount) {
                requireActivity().supportFragmentManager.popBackStack()
            }
            (activity as? MainActivity)?.navigateTo(GameFragment())
        }

        binding.btnMenu.setOnClickListener {
            repeat(requireActivity().supportFragmentManager.backStackEntryCount) {
                requireActivity().supportFragmentManager.popBackStack()
            }
            (activity as? MainActivity)?.navigateTo(MenuFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
