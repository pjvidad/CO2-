package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

        // --- Main Navigation ---
        binding.buttonCustomize.setOnClickListener {
            val shopFragment = ShopFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, shopFragment)
                .addToBackStack(null)
                .commit()
        }

        // --- Redeem Logic ---

        val redeemListener = View.OnClickListener { 
            val userPoints = binding.aquaPointsValue.text.toString().toIntOrNull() ?: 0
            val itemPrice = getString(R.string.price).toIntOrNull() ?: 0

            if (userPoints >= itemPrice) {
                Toast.makeText(requireContext(), "Item will be purchased when available.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Insufficient Balance.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.redeemBlue.setOnClickListener(redeemListener)
        binding.redeemGreen.setOnClickListener(redeemListener)
        binding.redeemFedora.setOnClickListener(redeemListener)
        binding.redeemGrey.setOnClickListener(redeemListener)
        binding.redeemLightpink.setOnClickListener(redeemListener)
        binding.redeemBibe.setOnClickListener(redeemListener)

        // --- Earn More Navigation ---
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
