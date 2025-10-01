package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class MonthlyFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tracker_monthly, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the button within the fragment's view
        val todayButton: Button = view.findViewById(R.id.todayButton)
        val weekButton: Button = view.findViewById(R.id.weekButton)

        // Set the click listener for the button
        todayButton.setOnClickListener {
            // This is the crucial part for navigation
            val trackerFragment = TrackerFragmentFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    trackerFragment
                ) // Replace the current fragment with EventFragment
                .addToBackStack(null) // IMPORTANT: This allows the user to press the back button to return to HomeFragment
                .commit()
        }
        weekButton.setOnClickListener {
            // This is the crucial part for navigation
            val weeklyFragment = WeeklyFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    weeklyFragment
                ) // Replace the current fragment with EventFragment
                .addToBackStack(null) // IMPORTANT: This allows the user to press the back button to return to HomeFragment
                .commit()
        }
    }
}