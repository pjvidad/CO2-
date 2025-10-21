package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.co2_.databinding.QuizNotifBinding

class QuizNotifFragment : Fragment() {

    private var _binding: QuizNotifBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = QuizNotifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide the bottom navigation bar
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav.visibility = View.GONE

        binding.nextButton2.setOnClickListener {
            val quizTextFragment = QuizTextFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, quizTextFragment)
                .addToBackStack(null)
                .commit()
        }

        // Add click listener for the exit button
        binding.exitButton2.setOnClickListener {
            // Send a result to the parent fragment (BookFragment)
            parentFragmentManager.setFragmentResult("lesson_exit", Bundle())
            // Pop the back stack all the way back to the library page
            requireActivity().supportFragmentManager.popBackStack("library_page", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // The navigation bar is made visible by the parent fragment (BookFragment)
    }
}
