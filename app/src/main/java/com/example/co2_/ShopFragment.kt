package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.co2_.databinding.ShopCustomizeBinding

class ShopFragment : Fragment() {

    private var _binding: ShopCustomizeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ShopCustomizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRedeem.setOnClickListener {
            // Create an instance of the fragment you want to switch to
            val shopRedeemFragment = ShopRedeemFragment()

            // Replace the current fragment with the new one
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, shopRedeemFragment) // Assumes your container is R.id.fragmentContainer
                .addToBackStack(null) // Allows user to press back to return
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
