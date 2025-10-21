package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.co2_.databinding.ShopRedeemBinding

class ShopRedeemFragment : Fragment() {

    private var _binding: ShopRedeemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ShopRedeemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCustomize.setOnClickListener {
            // Create an instance of the fragment you want to switch to
            val shopFragment = ShopFragment()

            // Replace the current fragment with the new one
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, shopFragment) // Make sure R.id.fragmentContainer is your main container
                .addToBackStack(null) // Optional: Adds the transaction to the back stack
                .commit()
        }

        binding.buttonQuiz.setOnClickListener {
            val bookFragment = BookFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, bookFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.buttonTasks.setOnClickListener {
            val homeFragment = HomeFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, homeFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.buttonFootprint.setOnClickListener {
            val trackerFragment = TrackerFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, trackerFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
