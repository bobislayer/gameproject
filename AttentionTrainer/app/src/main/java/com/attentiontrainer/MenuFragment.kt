package com.example.mobileproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.mobileproject.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Анимация появления
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)
        binding.root.startAnimation(fadeIn)

        val prefs = requireContext().getSharedPreferences("AttentionTrainer", 0)
        val bestScore = prefs.getInt("best_score", 0)
        if (bestScore > 0) {
            binding.tvBestScore.text = "Рекорд: $bestScore очков"
            binding.tvBestScore.visibility = View.VISIBLE
        }

        binding.btnStart.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(GameFragment())
        }

        binding.btnRules.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(RulesFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
