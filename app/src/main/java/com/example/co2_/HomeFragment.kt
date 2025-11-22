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
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.co2_.databinding.HomeTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class HomeFragment : Fragment() {

    private var _binding: HomeTaskBinding? = null
    private val binding get() = _binding!!

    private var taskViewToHide: View? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userDataListener: ListenerRegistration? = null

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

        if (auth.currentUser == null) {
            val intent = Intent(requireActivity(), WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
            return
        }

        db = FirebaseFirestore.getInstance()

        loadCachedUserData()
        listenForUserDataChanges()

        binding.profileImage.setOnClickListener { anchorView ->
            showPopupMenu(anchorView)
        }

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

    private fun showPopupMenu(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menu.add("Log Out")
        popup.setOnMenuItemClickListener { menuItem ->
            if (menuItem.title == "Log Out") {
                auth.signOut()
                val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()
                val intent = Intent(requireActivity(), WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
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

    private fun listenForUserDataChanges() {
        val user = auth.currentUser
        if (user != null) {
            userDataListener?.remove() // Avoid attaching multiple listeners
            userDataListener = db.collection("users").document(user.uid)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        Toast.makeText(requireContext(), "Error listening for user data: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        val photoUrl = document.getString("profile_picture")

                        // Update UI
                        binding.username.text = name
                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(this).load(photoUrl).into(binding.profileImage)
                        } else {
                            binding.profileImage.setImageResource(R.drawable.profile)
                        }

                        // Save fresh data to local cache
                        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putString("user_name", name)
                            putString("user_photo_url", photoUrl)
                            apply()
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
        // Launch the new Photo Picker
        pickVisualMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userDataListener?.remove() // Important: Remove the listener to prevent memory leaks
        _binding = null
    }
}
