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

        // Accessory Click Logic
        val accessoryImageViews = listOfNotNull(
            binding.accessoryImageView1,
            binding.accessoryImageView2,
            binding.accessoryImageView3,
            binding.accessoryImageView4,
            binding.accessoryImageView5,
            binding.accessoryImageView6
        )

        accessoryImageViews.forEach { currentImageView ->
            currentImageView.setOnClickListener { clickedView ->
                lastSelectedImageView?.setBackgroundResource(R.drawable.card_background)
                clickedView.setBackgroundResource(R.drawable.selected_card_background)
                selectedAccessoryResId = clickedView.tag as? Int
                lastSelectedImageView = clickedView as ImageView
            }
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