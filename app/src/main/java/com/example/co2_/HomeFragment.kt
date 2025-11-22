package com.example.co2_

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.co2_.databinding.HomeTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: HomeTaskBinding? = null
    private val binding get() = _binding!!

    private var taskViewToHide: View? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "Image successfully verified.", Toast.LENGTH_SHORT).show()
            taskViewToHide?.visibility = View.GONE
        } else {
            Toast.makeText(requireContext(), "There was no image verified.", Toast.LENGTH_SHORT).show()
        }
    }

    // The new Photo Picker launcher
    private val pickVisualMediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Toast.makeText(requireContext(), "Image successfully verified.", Toast.LENGTH_SHORT).show()
            taskViewToHide?.visibility = View.GONE
        } else {
            Toast.makeText(requireContext(), "There was no image verified.", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            navigateToWelcome()
            return
        }

        checkUserVerification(currentUser)
    }

    private fun checkUserVerification(user: FirebaseUser) {
        val isEmailPasswordUser = user.providerData.any { it.providerId == "password" }

        if (!isEmailPasswordUser || user.isEmailVerified) {
            initializeUI()
            return
        }

        user.reload().addOnCompleteListener { task ->
            if (!isAdded) return@addOnCompleteListener

            if (task.isSuccessful && auth.currentUser?.isEmailVerified == true) {
                initializeUI()
            } else {
                val message = if (task.isSuccessful) {
                    "Please verify your email address to continue."
                } else {
                    "Offline: Could not check verification status. Please connect to the internet."
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                auth.signOut()
                navigateToWelcome()
            }
        }
    }

    private fun initializeUI() {
        db = FirebaseFirestore.getInstance()

        loadCachedUserData()
        loadUserData()

        binding.profileImage.setOnClickListener { anchorView ->
            showPopupMenu(anchorView)
        }

        binding.eventsButton.setOnClickListener {
            val eventFragment = EventFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, eventFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.task0Button.setOnClickListener {
            taskViewToHide = binding.timelineTaskItem0
            showProofDialog()
        }

        binding.task1Button.setOnClickListener {
            taskViewToHide = binding.timelineTaskItem1
            showProofDialog()
        }
    }

    private fun navigateToWelcome() {
        if (!isAdded || requireActivity().isFinishing) {
            return
        }
        val intent = Intent(requireActivity(), WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }


    private fun showPopupMenu(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menu.add("Log Out")
        popup.setOnMenuItemClickListener { menuItem ->
            if (menuItem.title == "Log Out") {
                auth.signOut()
                requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).edit {
                    clear()
                }
                navigateToWelcome()
                true
            } else {
                false
            }
        }
        popup.show()
    }

    private fun loadCachedUserData() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("user_name", "User")
        val photoUrl = sharedPreferences.getString("user_photo_url", null)

        binding.username.text = name
        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this).load(photoUrl).into(binding.profileImage)
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (isAdded && document != null && document.exists()) {
                        val name = document.getString("name")
                        val photoUrl = document.getString("profile_picture")
                        val aquaPoints = document.getLong("aqua_points")?.toInt() ?: 0
                        val dailyImpact = document.getDouble("daily_impact") ?: 0.0
                        val monthlyImpact = document.getDouble("monthly_impact") ?: 0.0

                        // Update UI
                        binding.username.text = name
                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(this).load(photoUrl).into(binding.profileImage)
                        } else {
                            binding.profileImage.setImageResource(R.drawable.profile)
                        }

                        // Save fresh data to local cache
                        requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).edit {
                            putString("user_name", name)
                            putString("user_photo_url", photoUrl)
                            putInt("aqua_points", aquaPoints)
                            putFloat("daily_impact", dailyImpact.toFloat())
                            putFloat("monthly_impact", monthlyImpact.toFloat())
                        }
                    }
                }
        }
    }

    private fun showProofDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Prove your action")
            .setMessage("Choose where to show your proof.")
            .setPositiveButton("Camera") { _, _ -> checkCameraPermission() }
            .setNeutralButton("Gallery") { _, _ -> openGallery() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> openCamera()
            else -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(intent)
    }

    private fun openGallery() {
        pickVisualMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
