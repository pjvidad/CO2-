package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.home_task, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the button within the fragment's view
        val eventButton: Button = view.findViewById(R.id.eventsButton)

        // Set the click listener for the button
        eventButton.setOnClickListener {
            // This is the crucial part for navigation
            val eventFragment = EventFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    eventFragment
                ) // Replace the current fragment with EventFragment
                .addToBackStack(null) // IMPORTANT: This allows the user to press the back button to return to HomeFragment
                .commit()
        }
    }
}
