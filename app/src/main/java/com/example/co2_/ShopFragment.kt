package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.co2_.databinding.ShopCustomizeBinding // Ensure this matches your layout file name

class ShopFragment : Fragment() { // This is your Customize screen

    // Stores the resource ID of the *full mascot image with the selected accessory*
    private var selectedMascotImageResId: Int? = null
    private var lastSelectedIconImageView: ImageView? = null // Tracks the clicked icon
    private var _binding: ShopCustomizeBinding? = null
    private val binding get() = _binding!!

    // Store the resource ID of your default, plain mascot image
    private val defaultMascotImageResId = R.drawable.el_hewooo // Replace with your actual default mascot drawable

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
            val shopRedeemFragment = ShopRedeemFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, shopRedeemFragment)
                .commit()
        }

        // *** CRITICAL: Update this map ***
        // Keys are the ICON ImageView IDs.
        // Values are the corresponding FULL mascot image (wearing the accessory) drawable IDs.
        val accessoryMap = mapOf(
            binding.accessoryImageView1.id to R.drawable.mascot_wearing_white,   // Example: R.drawable.mascot_wearing_white
            binding.accessoryImageView2.id to R.drawable.mascot_wearing_violet,  // Example: R.drawable.mascot_wearing_violet
            binding.accessoryImageView3.id to R.drawable.mascot_wearing_grey,    // Example: R.drawable.mascot_wearing_grey
            binding.accessoryImageView4.id to R.drawable.mascot_wearing_blue,    // Example: R.drawable.mascot_wearing_blue
            binding.accessoryImageView5.id to R.drawable.mascot_wearing_fedora,  // Example: R.drawable.mascot_wearing_fedora
            binding.accessoryImageView6.id to R.drawable.mascot_wearing_bibe     // Example: R.drawable.mascot_wearing_bibe
        )

        val accessoryIconImageViews = accessoryMap.keys.mapNotNull { view.findViewById<ImageView>(it) }

        val clickListener = View.OnClickListener { clickedIconView ->
            lastSelectedIconImageView?.setBackgroundResource(R.drawable.card_background)
            clickedIconView.setBackgroundResource(R.drawable.selected_card_background)
            // Store the ID of the full mascot image corresponding to the clicked icon
            selectedMascotImageResId = accessoryMap[clickedIconView.id]
            lastSelectedIconImageView = clickedIconView as ImageView
        }

        accessoryIconImageViews.forEach { iconImageView ->
            iconImageView.setOnClickListener(clickListener)
        }

        binding.buttonDone.setOnClickListener {
            selectedMascotImageResId?.let { fullMascotResId ->
                // Set the main mascot image to the selected full image
                binding.mascotImage.setImageResource(fullMascotResId)
                Toast.makeText(context, "Accessory Applied!", Toast.LENGTH_SHORT).show()
            } ?: run {
                // If nothing selected, revert to the default mascot image
                binding.mascotImage.setImageResource(defaultMascotImageResId)
                Toast.makeText(context, "No accessory selected. Reverting to default.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}