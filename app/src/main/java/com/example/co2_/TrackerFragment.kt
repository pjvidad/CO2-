package com.example.co2_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.co2_.databinding.TrackerDailyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

class TrackerFragment : Fragment() {

    private var _binding: TrackerDailyBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var impactListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TrackerDailyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnAdd2.setOnClickListener {
            val addEntryFragment = AddEntryFragment()
            addEntryFragment.show(parentFragmentManager, "AddEntryDialog")
        }

        binding.fabMap.setOnClickListener {
            val mapFragment = TrackerMapFragment()
            mapFragment.show(parentFragmentManager, "TrackerMapDialog")
        }

        listenForImpactDataChanges()
    }

    private fun listenForImpactDataChanges() {
        val userId = auth.currentUser?.uid ?: return

        impactListener?.remove() // Avoid attaching multiple listeners
        impactListener = db.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val dailyImpact = document.getDouble("daily_impact") ?: 0.0
                    val monthlyImpact = document.getDouble("monthly_impact") ?: 0.0

                    binding.impactValue2.text = String.format(Locale.getDefault(), "%.1f kg CO2", dailyImpact)
                    binding.monthlyValue.text = String.format(Locale.getDefault(), "%.1f kg used", monthlyImpact)

                    // You can add logic for the comparison text and progress bar here
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        impactListener?.remove() // Important: Remove the listener to prevent memory leaks
        _binding = null
    }
}
