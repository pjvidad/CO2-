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
import java.util.Calendar
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
            loadCachedImpactData()
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

        checkForDailyReset()
    }

    private fun checkForDailyReset() {
        val prefs = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val lastUpdateMillis = prefs.getLong("last_update_timestamp", 0)
        val lastUpdateCalendar = Calendar.getInstance().apply { timeInMillis = lastUpdateMillis }
        val currentCalendar = Calendar.getInstance()

        val isNewDay = currentCalendar.get(Calendar.DAY_OF_YEAR) > lastUpdateCalendar.get(Calendar.DAY_OF_YEAR) ||
                       currentCalendar.get(Calendar.YEAR) > lastUpdateCalendar.get(Calendar.YEAR)

        if (isNewDay) {
            val lastDailyImpact = prefs.getFloat("daily_impact", 0f)
            
            prefs.edit {
                putFloat("yesterdays_impact", lastDailyImpact)
                putFloat("daily_impact", 0f)
                putFloat("transport_impact", 0f)
                putFloat("energy_impact", 0f)
                putFloat("shopping_impact", 0f)
                putLong("last_update_timestamp", currentCalendar.timeInMillis)
            }

            // Also reset in Firebase
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val userDocRef = db.collection("users").document(userId)
                userDocRef.update(mapOf(
                    "daily_impact" to 0.0,
                    "transport_impact" to 0.0,
                    "energy_impact" to 0.0,
                    "shopping_impact" to 0.0
                )).addOnCompleteListener { 
                    // Once Firebase is updated, ensure the UI reflects the very latest state.
                    loadImpactData() 
                }
            } else {
                 // If user is null, just load from cache which is already reset
                 loadCachedImpactData()
            }
        } else {
             // Not a new day, just load data as usual
            loadCachedImpactData()
            loadImpactData()
        }
    }


    private fun loadCachedImpactData() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val dailyImpact = sharedPreferences.getFloat("daily_impact", 0f).toDouble()
        val monthlyImpact = sharedPreferences.getFloat("monthly_impact", 0f).toDouble()
        val transportImpact = sharedPreferences.getFloat("transport_impact", 0f).toDouble()
        val energyImpact = sharedPreferences.getFloat("energy_impact", 0f).toDouble()
        val shoppingImpact = sharedPreferences.getFloat("shopping_impact", 0f).toDouble()
        val yesterdaysImpact = sharedPreferences.getFloat("yesterdays_impact", 0f).toDouble()

        updateUI(dailyImpact, monthlyImpact, transportImpact, energyImpact, shoppingImpact, yesterdaysImpact)
    }

    private fun loadImpactData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            // No need to call navigateToWelcome, as checkForDailyReset will handle loading from cache
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
                    
                    // "yesterdays_impact" is not stored in Firestore, it's a local concept.
                    val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                    val yesterdaysImpact = sharedPreferences.getFloat("yesterdays_impact", 0f).toDouble()

                    updateUI(dailyImpact, monthlyImpact, transportImpact, energyImpact, shoppingImpact, yesterdaysImpact)

                    sharedPreferences.edit {
                        putFloat("daily_impact", dailyImpact.toFloat())
                        putFloat("monthly_impact", monthlyImpact.toFloat())
                        putFloat("transport_impact", transportImpact.toFloat())
                        putFloat("energy_impact", energyImpact.toFloat())
                        putFloat("shopping_impact", shoppingImpact.toFloat())
                        // Also update timestamp on successful fetch
                        putLong("last_update_timestamp", System.currentTimeMillis())
                    }
                } 
            }
    }

    private fun updateUI(dailyImpact: Double, monthlyImpact: Double, transportImpact: Double, energyImpact: Double, shoppingImpact: Double, yesterdaysImpact: Double) {
        // Today's Impact
        binding.impactValue2.text = String.format(Locale.getDefault(), "%.1f kg CO2", dailyImpact)

        val denominator = if (yesterdaysImpact == 0.0) 1.0 else yesterdaysImpact
        val percentage = ((dailyImpact - denominator) / denominator) * 100

        if (percentage > 0) {
            binding.impactComparison2.text = String.format(Locale.getDefault(), "%.0f%% above yesterday", percentage)
            binding.impactComparison2.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        } else if (percentage < 0) {
            binding.impactComparison2.text = String.format(Locale.getDefault(), "%.0f%% below yesterday", -percentage)
            binding.impactComparison2.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
        }
        else {
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
