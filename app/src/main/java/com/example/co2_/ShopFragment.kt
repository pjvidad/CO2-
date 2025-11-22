package com.example.co2_

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.co2_.databinding.ShopCustomizeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ShopFragment : Fragment() { // This is your Customize screen

    private var selectedMascotImageResId: Int? = null
    private var lastSelectedIconImageView: ImageView? = null
    private var _binding: ShopCustomizeBinding? = null
    private val binding get() = _binding!!

    private val defaultMascotImageResId = R.drawable.el_hewooo

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ShopCustomizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadCachedAquaPoints()
        loadAquaPoints()

        binding.buttonRedeem.setOnClickListener {
            val shopRedeemFragment = ShopRedeemFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, shopRedeemFragment)
                .commit()
        }

        val accessoryMap = mapOf(
            binding.accessoryImageView1.id to R.drawable.mascot_wearing_white,
            binding.accessoryImageView2.id to R.drawable.mascot_wearing_violet,
            binding.accessoryImageView3.id to R.drawable.mascot_wearing_grey,
            binding.accessoryImageView4.id to R.drawable.mascot_wearing_blue,
            binding.accessoryImageView5.id to R.drawable.mascot_wearing_fedora,
            binding.accessoryImageView6.id to R.drawable.mascot_wearing_bibe
        )

        val accessoryIconImageViews = accessoryMap.keys.mapNotNull { view.findViewById<ImageView>(it) }

        val clickListener = View.OnClickListener { clickedIconView ->
            lastSelectedIconImageView?.setBackgroundResource(R.drawable.card_background)
            clickedIconView.setBackgroundResource(R.drawable.selected_card_background)
            selectedMascotImageResId = accessoryMap[clickedIconView.id]
            lastSelectedIconImageView = clickedIconView as ImageView
        }

        accessoryIconImageViews.forEach { iconImageView ->
            iconImageView.setOnClickListener(clickListener)
        }

        binding.buttonDone.setOnClickListener {
            selectedMascotImageResId?.let { fullMascotResId ->
                binding.mascotImage.setImageResource(fullMascotResId)
                Toast.makeText(context, "Accessory Applied!", Toast.LENGTH_SHORT).show()
            } ?: run {
                binding.mascotImage.setImageResource(defaultMascotImageResId)
                Toast.makeText(context, "No accessory selected. Reverting to default.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCachedAquaPoints() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val aquaPoints = sharedPreferences.getInt("aqua_points", 0)
        binding.aquaPointsValue.text = aquaPoints.toString()
    }

    private fun loadAquaPoints() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            navigateToWelcome()
            return
        }

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (isAdded && document != null && document.exists()) {
                    val aquaPoints = document.getLong("aqua_points")?.toInt() ?: 0
                    binding.aquaPointsValue.text = aquaPoints.toString()

                    requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).edit {
                        putInt("aqua_points", aquaPoints)
                    }
                } 
            }
    }

    private fun navigateToWelcome() {
        if (!isAdded || activity?.isFinishing == true) {
            return
        }
        val intent = Intent(activity, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
