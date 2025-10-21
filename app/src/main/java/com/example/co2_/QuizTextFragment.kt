package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.co2_.databinding.QuizTextBinding

class QuizTextFragment : Fragment() {

    private var _binding: QuizTextBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = QuizTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide the bottom navigation bar
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav.visibility = View.GONE

        // Add click listener for the exit button
        binding.exitButton6.setOnClickListener {
            // Send a result to the parent fragment (BookFragment)
            parentFragmentManager.setFragmentResult("lesson_exit", Bundle())
            // Pop the back stack all the way back to the library page
            requireActivity().supportFragmentManager.popBackStack("library_page", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        // Set up button navigation
        val quizFalseFragment = QuizFalseFragment()
        val quizImageFragment = QuizImageFragment()

        binding.button.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, quizFalseFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.button2.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, quizFalseFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.button3.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, quizImageFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.button4.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, quizFalseFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // The navigation bar is made visible by the parent fragment (BookFragment)
    }
}
