package com.example.co2_

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.co2_.databinding.HomeTaskBinding
import com.google.firebase.auth.FirebaseAuth
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
            // The user has selected an image. The URI is available here.
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
        db = FirebaseFirestore.getInstance()

        loadUserData()

        binding.eventsButton.setOnClickListener {
            val eventFragment = EventFragment()
            requireActivity().supportFragmentManager.beginTransaction()
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

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        binding.username.text = document.getString("name")
                        val photoUrl = document.getString("profile_picture")
                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(this).load(photoUrl).into(binding.profileImage)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error getting user data: ${exception.message}", Toast.LENGTH_SHORT).show()
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
        // Launch the new Photo Picker
        pickVisualMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
