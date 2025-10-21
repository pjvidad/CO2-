package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.co2_.databinding.ShopCustomizeBinding

class ShopFragment : Fragment() {

    private var selectedAccessoryResId: Int? = null
    private var lastSelectedImageView: ImageView? = null
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
            val shopRedeemFragment = ShopRedeemFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, shopRedeemFragment)
                .commit()
        }

        // --- Accessory Click Logic ---

        // A map to associate each ImageView with its drawable resource.
        // This is much safer and more reliable than using tags.
        val accessoryMap = mapOf(
            binding.accessoryImageView1.id to R.drawable.white,
            binding.accessoryImageView2.id to R.drawable.violet,
            binding.accessoryImageView3.id to R.drawable.grey,
            binding.accessoryImageView4.id to R.drawable.blue,
            binding.accessoryImageView5.id to R.drawable.fedora,
            binding.accessoryImageView6.id to R.drawable.bibe
        )

        val accessoryImageViews = accessoryMap.keys.map { view.findViewById<ImageView>(it) }

        // A single, reusable click listener for all accessories
        val clickListener = View.OnClickListener { clickedView ->
            // Revert the background of the previously selected item
            lastSelectedImageView?.setBackgroundResource(R.drawable.card_background)

            // Highlight the new selection
            clickedView.setBackgroundResource(R.drawable.selected_card_background)

            // Store the drawable resource ID for the selected accessory using the map
            selectedAccessoryResId = accessoryMap[clickedView.id]

            // Keep track of the last selected view
            lastSelectedImageView = clickedView as ImageView
        }

        accessoryImageViews.forEach { imageView ->
            imageView.setOnClickListener(clickListener)
        }

        binding.buttonDone.setOnClickListener {
            selectedAccessoryResId?.let { resId ->
                binding.mascotAccessoryLayer.setImageResource(resId)
                binding.mascotAccessoryLayer.visibility = View.VISIBLE
                Toast.makeText(context, "Accessory Applied!", Toast.LENGTH_SHORT).show()
            } ?: run {
                binding.mascotAccessoryLayer.visibility = View.GONE
                Toast.makeText(context, "No accessory selected.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}