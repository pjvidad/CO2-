package com.example.co2_

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.co2_.databinding.TrackerDailyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class TrackerFragment : Fragment() {

    private var _binding: TrackerDailyBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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

        loadCachedImpactData()
        loadImpactData()
    }

    private fun loadCachedImpactData() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val dailyImpact = sharedPreferences.getFloat("daily_impact", 0f)
        val monthlyImpact = sharedPreferences.getFloat("monthly_impact", 0f)

        binding.impactValue2.text = String.format(Locale.getDefault(), "%.1f kg CO2", dailyImpact)
        binding.monthlyValue.text = String.format(Locale.getDefault(), "%.1f kg used", monthlyImpact)
    }

    private fun loadImpactData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            navigateToWelcome()
            return
        }

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (isAdded && document != null && document.exists()) {
                    val dailyImpact = document.getDouble("daily_impact") ?: 0.0
                    val monthlyImpact = document.getDouble("monthly_impact") ?: 0.0

                    binding.impactValue2.text = String.format(Locale.getDefault(), "%.1f kg CO2", dailyImpact)
                    binding.monthlyValue.text = String.format(Locale.getDefault(), "%.1f kg used", monthlyImpact)

                    requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).edit {
                        putFloat("daily_impact", dailyImpact.toFloat())
                        putFloat("monthly_impact", monthlyImpact.toFloat())
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
