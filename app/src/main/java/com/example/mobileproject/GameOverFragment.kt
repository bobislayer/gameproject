package com.example.mobileproject

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.mobileproject.databinding.FragmentGameOverBinding

class GameOverFragment : Fragment() {
    private var _binding: FragmentGameOverBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(score: Int, level: Int) = GameOverFragment().apply {
            arguments = Bundle().apply { putInt("score", score); putInt("level", level) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameOverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val score = arguments?.getInt("score") ?: 0
        val level = arguments?.getInt("level") ?: 1
        binding.tvScore.text = "$score"
        binding.tvLevel.text = "Достигнут уровень $level"
        val best = requireContext().getSharedPreferences("AttentionTrainer", 0).getInt("best_score", 0)
        if (score >= best && score > 0) {
            binding.tvBest.text = "Новый рекорд!"
            binding.tvBest.visibility = View.VISIBLE
        }
        binding.btnRestart.setOnClickListener {
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

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
