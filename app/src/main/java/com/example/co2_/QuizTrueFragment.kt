package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.co2_.databinding.QuizTrueBinding

class QuizTrueFragment : Fragment() {

    private var _binding: QuizTrueBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = QuizTrueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide the bottom navigation bar
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav.visibility = View.GONE

        // Create a single listener to exit the quiz
        val exitListener = View.OnClickListener {
            // Send the result to BookFragment to lower the score
            parentFragmentManager.setFragmentResult("lesson_exit", Bundle())
            // Pop the back stack all the way back to the library page
            requireActivity().supportFragmentManager.popBackStack("library_page", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        // Assign the listener to both buttons
        binding.exitButton4.setOnClickListener(exitListener)
        binding.nextButton3.setOnClickListener(exitListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // The navigation bar is made visible by the parent fragment (BookFragment)
    }
}
