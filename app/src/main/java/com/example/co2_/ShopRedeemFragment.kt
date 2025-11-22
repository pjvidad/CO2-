package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.co2_.databinding.ShopRedeemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ShopRedeemFragment : Fragment() {

    private var _binding: ShopRedeemBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var aquaPointsListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ShopRedeemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        listenForAquaPointsChanges()

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
