package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.co2_.databinding.ShopCustomizeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ShopFragment : Fragment() { // This is your Customize screen

    private var selectedMascotImageResId: Int? = null
    private var lastSelectedIconImageView: ImageView? = null
    private var _binding: ShopCustomizeBinding? = null
    private val binding get() = _binding!!

    private val defaultMascotImageResId = R.drawable.el_hewooo

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var aquaPointsListener: ListenerRegistration? = null

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

        listenForAquaPointsChanges()

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

    private fun listenForAquaPointsChanges() {
        val userId = auth.currentUser?.uid ?: return

        aquaPointsListener?.remove() // Avoid attaching multiple listeners
        aquaPointsListener = db.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val aquaPoints = document.getLong("aqua_points")?.toInt() ?: 0
                    binding.aquaPointsValue.text = aquaPoints.toString()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        aquaPointsListener?.remove() // Important: Remove the listener to prevent memory leaks
        _binding = null
    }
}
