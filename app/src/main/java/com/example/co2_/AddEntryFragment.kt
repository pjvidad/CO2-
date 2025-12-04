package com.example.co2_

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.example.co2_.databinding.FragmentAddEntryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddEntryFragment : DialogFragment() {

    private var _binding: FragmentAddEntryBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val transportModes = arrayOf(
        "Jeepney (Traditional)",
        "Modern Jeep / UV Express",
        "City Bus (Ordinary)",
        "City Bus (Aircon/P2P)",
        "MRT / LRT / PNR",
        "Tricycle",
        "Motorcycle (Solo)",
        "Private Car (Sedan/Gas)",
        "Private Car (SUV/Diesel)"
    )

    private val energySources = arrayOf(
        "Grid Electricity (Luzon/Visayas)",
        "Grid Electricity (Mindanao)",
        "LPG (Cooking Gas)",
        "Diesel Generator",
        "Solar Panels (Home)"
    )

    private val shoppingItems = arrayOf(
        "Cotton T-Shirt",
        "Denim Jeans",
        "Thick Jacket/Coat",
        "Leather Shoes",
        "Smartphone",
        "Local Crafts (Wood/Weave)"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEntryBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val transportAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, transportModes)
        (binding.autoCompleteTransportMode as? AutoCompleteTextView)?.setAdapter(transportAdapter)

        val energyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, energySources)
        (binding.autoCompleteEnergySource as? AutoCompleteTextView)?.setAdapter(energyAdapter)

        val shoppingItemAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, shoppingItems)
        (binding.autoCompleteItemCategory as? AutoCompleteTextView)?.setAdapter(shoppingItemAdapter)

        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: View.NO_ID
            binding.transportOptionsLayout.visibility = if (checkedId == R.id.chip_transport) View.VISIBLE else View.GONE
            binding.energyOptionsLayout.visibility = if (checkedId == R.id.chip_energy) View.VISIBLE else View.GONE
            binding.shoppingOptionsLayout.visibility = if (checkedId == R.id.chip_shopping) View.VISIBLE else View.GONE
        }

        binding.buttonClose.setOnClickListener {
            dismiss()
        }

        binding.buttonSaveEntry.setOnClickListener {
            saveEntry()
        }

        return binding.root
    }

    private fun getEmissionFactor(mode: String, unit: Int): Double {
        return when (unit) {
            R.id.chip_km -> getEmissionFactorKm(mode)
            R.id.chip_miles -> getEmissionFactorMiles(mode)
            else -> 0.0
        }
    }

    private fun getEmissionFactorKm(mode: String): Double {
        return when (mode) {
            "Jeepney (Traditional)" -> 0.04
            "Modern Jeep / UV Express" -> 0.05
            "City Bus (Ordinary)" -> 0.035
            "City Bus (Aircon/P2P)" -> 0.055
            "MRT / LRT / PNR" -> 0.05
            "Tricycle" -> 0.08
            "Motorcycle (Solo)" -> 0.08
            "Private Car (Sedan/Gas)" -> 0.53
            "Private Car (SUV/Diesel)" -> 0.58
            else -> 0.0
        }
    }

    private fun getEmissionFactorMiles(mode: String): Double {
        return when (mode) {
            "Jeepney (Traditional)" -> 0.09
            "Modern Jeep / UV Express" -> 0.11
            "City Bus (Ordinary)" -> 0.08
            "City Bus (Aircon/P2P)" -> 0.12
            "MRT / LRT / PNR" -> 0.11
            "Tricycle" -> 0.18
            "Motorcycle (Solo)" -> 0.18
            "Private Car (Sedan/Gas)" -> 1.16
            "Private Car (SUV/Diesel)" -> 1.28
            else -> 0.0
        }
    }

    private fun getEnergyEmissionFactor(source: String): Double {
        return when (source) {
            "Grid Electricity (Luzon/Visayas)" -> 0.70
            "Grid Electricity (Mindanao)" -> 0.50
            "LPG (Cooking Gas)" -> 3.0
            "Diesel Generator" -> 2.7
            "Solar Panels (Home)" -> 0.04
            else -> 0.0
        }
    }

    private fun getShoppingEmissionFactor(item: String, condition: Int): Double {
        val isBrandNew = condition == R.id.chip_brand_new
        return when (item) {
            "Cotton T-Shirt" -> if (isBrandNew) 7.0 else 0.3
            "Denim Jeans" -> if (isBrandNew) 33.0 else 0.8
            "Thick Jacket/Coat" -> if (isBrandNew) 25.0 else 1.2
            "Leather Shoes" -> if (isBrandNew) 15.0 else 1.5
            "Smartphone" -> 65.0
            "Local Crafts (Wood/Weave)" -> if (isBrandNew) 2.0 else 0.0
            else -> 0.0
        }
    }

    private fun saveEntry() {
        val amountStr = binding.editTextAmount.text.toString()
        if (amountStr.isEmpty()) {
            Toast.makeText(context, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountStr.toDouble()
        var carbonFootprint = 0.0
        var category = ""

        val checkedCategoryId = binding.chipGroupCategory.checkedChipIds.firstOrNull()

        when (checkedCategoryId) {
            R.id.chip_transport -> {
                val selectedUnit = binding.chipGroupUnits.checkedChipId
                val selectedMode = binding.autoCompleteTransportMode.text.toString()

                if (selectedUnit == -1 || selectedMode.isEmpty()) {
                    Toast.makeText(context, "Please select unit and mode of transport", Toast.LENGTH_SHORT).show()
                    return
                }

                val emissionFactor = getEmissionFactor(selectedMode, selectedUnit)
                carbonFootprint = amount * emissionFactor
                category = "transport_impact"
            }
            R.id.chip_energy -> {
                val selectedSource = binding.autoCompleteEnergySource.text.toString()
                if (selectedSource.isEmpty()) {
                    Toast.makeText(context, "Please select an energy source", Toast.LENGTH_SHORT).show()
                    return
                }

                val emissionFactor = getEnergyEmissionFactor(selectedSource)
                carbonFootprint = amount * emissionFactor
                category = "energy_impact"
            }
            R.id.chip_shopping -> {
                val selectedItem = binding.autoCompleteItemCategory.text.toString()
                val selectedCondition = binding.chipGroupCondition.checkedChipId

                if (selectedItem.isEmpty() || selectedCondition == -1) {
                    Toast.makeText(context, "Please select item category and condition", Toast.LENGTH_SHORT).show()
                    return
                }

                val emissionFactor = getShoppingEmissionFactor(selectedItem, selectedCondition)
                carbonFootprint = amount * emissionFactor
                category = "shopping_impact"
            }
            else -> {
                Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // --- Optimistic UI Update ---
        val prefs = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val newDailyImpact = prefs.getFloat("daily_impact", 0f) + carbonFootprint.toFloat()
        val newMonthlyImpact = prefs.getFloat("monthly_impact", 0f) + carbonFootprint.toFloat()
        val newCategoryImpact = prefs.getFloat(category, 0f) + carbonFootprint.toFloat()

        prefs.edit {
            putFloat("daily_impact", newDailyImpact)
            putFloat("monthly_impact", newMonthlyImpact)
            putFloat(category, newCategoryImpact)
            putLong("last_update_timestamp", System.currentTimeMillis())
        }

        Toast.makeText(context, "Entry saved!", Toast.LENGTH_SHORT).show()
        parentFragmentManager.setFragmentResult("entry_saved", Bundle())
        dismiss()

        // --- Firebase Background Sync ---
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userDocRef = db.collection("users").document(userId)
            val updates = mapOf(
                "daily_impact" to FieldValue.increment(carbonFootprint),
                "monthly_impact" to FieldValue.increment(carbonFootprint),
                category to FieldValue.increment(carbonFootprint)
            )
            userDocRef.update(updates).addOnFailureListener { e ->
                Log.w("AddEntryFragment", "Failed to sync impact update to Firebase", e)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
