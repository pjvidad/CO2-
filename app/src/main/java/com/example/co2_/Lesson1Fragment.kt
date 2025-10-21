package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.co2_.databinding.Lesson1Binding

class Lesson1Fragment : Fragment() {

    private var _binding: Lesson1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Lesson1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide the bottom navigation bar
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav.visibility = View.GONE

        binding.nextButton.setOnClickListener {
            val quizNotifFragment = QuizNotifFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, quizNotifFragment)
                .addToBackStack(null)
                .commit()
        }

        // Add click listener for the exit button
        binding.exitButton.setOnClickListener {
            // Go back to the previous screen (BookFragment)
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show the bottom navigation bar again when the fragment is destroyed
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav.visibility = View.VISIBLE
        _binding = null
    }
}
