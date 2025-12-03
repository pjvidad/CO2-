package com.example.co2_

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

        parentFragmentManager.setFragmentResultListener("entry_saved", this) { _, _ ->
            loadImpactData()
        }

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
        val dailyImpact = sharedPreferences.getFloat("daily_impact", 0f).toDouble()
        val monthlyImpact = sharedPreferences.getFloat("monthly_impact", 0f).toDouble()
        val transportImpact = sharedPreferences.getFloat("transport_impact", 0f).toDouble()
        val energyImpact = sharedPreferences.getFloat("energy_impact", 0f).toDouble()
        val shoppingImpact = sharedPreferences.getFloat("shopping_impact", 0f).toDouble()

        updateUI(dailyImpact, monthlyImpact, transportImpact, energyImpact, shoppingImpact)
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
                    val transportImpact = document.getDouble("transport_impact") ?: 0.0
                    val energyImpact = document.getDouble("energy_impact") ?: 0.0
                    val shoppingImpact = document.getDouble("shopping_impact") ?: 0.0

                    updateUI(dailyImpact, monthlyImpact, transportImpact, energyImpact, shoppingImpact)

                    requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).edit {
                        putFloat("daily_impact", dailyImpact.toFloat())
                        putFloat("monthly_impact", monthlyImpact.toFloat())
                        putFloat("transport_impact", transportImpact.toFloat())
                        putFloat("energy_impact", energyImpact.toFloat())
                        putFloat("shopping_impact", shoppingImpact.toFloat())
                    }
                } 
            }
    }

    private fun updateUI(dailyImpact: Double, monthlyImpact: Double, transportImpact: Double, energyImpact: Double, shoppingImpact: Double) {
        // Today's Impact
        binding.impactValue2.text = String.format(Locale.getDefault(), "%.1f kg CO2", dailyImpact)

        val yesterdaysImpact = 1.0

        if (dailyImpact > yesterdaysImpact) {
            val percentage = if (yesterdaysImpact >= 0) {
                ((dailyImpact - yesterdaysImpact) / yesterdaysImpact) * 100
            } else {
                100.0
            }
            binding.impactComparison2.text = String.format(Locale.getDefault(), "%.0f%% above yesterday", percentage)
            binding.impactComparison2.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        } else {
            binding.impactComparison2.text = "0% vs yesterday"
            binding.impactComparison2.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
        }

        // Category Breakdown
        binding.transportValue.text = String.format(Locale.getDefault(), "%.1f kg", transportImpact)
        binding.energyValue.text = String.format(Locale.getDefault(), "%.1f kg", energyImpact)
        binding.shoppingValue.text = String.format(Locale.getDefault(), "%.1f kg", shoppingImpact)

        // Monthly Goal
        val monthlyGoal = 150.0
        val remaining = monthlyGoal - monthlyImpact
        val monthlyProgress = if (monthlyGoal > 0) {
            ((monthlyImpact / monthlyGoal) * 100).toInt()
        } else {
            0
        }

        binding.monthlyProgressBar.progress = monthlyProgress.coerceIn(0, 100)
        binding.monthlyUsedValue.text = String.format(Locale.getDefault(), "%.1f kg used", monthlyImpact)
        binding.monthlyGoalValue.text = String.format(Locale.getDefault(), "/ %.1f kg goal", monthlyGoal)
        binding.monthlyValue.text = String.format(Locale.getDefault(), "%.1f kg", remaining)
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
