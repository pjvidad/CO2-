package com.example.co2_

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.co2_.databinding.HomeTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class HomeFragment : Fragment() {

    private var _binding: HomeTaskBinding? = null
    private val binding get() = _binding!!

    private var taskViewToHide: View? = null
    private var latestTmpUri: Uri? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            latestTmpUri?.let {
                uploadProofImage(it)
                Toast.makeText(requireContext(), "Image successfully verified.", Toast.LENGTH_SHORT).show()
                markTaskAsCompleted()
                taskViewToHide?.visibility = View.GONE
                addAquaPoints(10)
            }
        } else {
            Toast.makeText(requireContext(), "There was no image verified.", Toast.LENGTH_SHORT).show()
        }
    }

    // The new Photo Picker launcher
    private val pickVisualMediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uploadProofImage(uri)
            Toast.makeText(requireContext(), "Image successfully verified.", Toast.LENGTH_SHORT).show()
            markTaskAsCompleted()
            taskViewToHide?.visibility = View.GONE
            addAquaPoints(50)
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
        storage = FirebaseStorage.getInstance()
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

        val sharedPreferences = requireActivity().getSharedPreferences("task_completion", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("task_0_completed", false)) {
            binding.timelineTaskItem0.visibility = View.GONE
        }
        if (sharedPreferences.getBoolean("task_1_completed", false)) {
            binding.timelineTaskItem1.visibility = View.GONE
        }

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
                requireActivity().getSharedPreferences("task_completion", Context.MODE_PRIVATE).edit {
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

    private fun uploadProofImage(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val fileName = "proof_${System.currentTimeMillis()}.jpg"
        val storageRef = storage.reference.child("tasks/$userId/$fileName")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Proof uploaded successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to upload proof: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun markTaskAsCompleted() {
        val taskKey = when (taskViewToHide?.id) {
            R.id.timeline_task_item_0 -> "task_0_completed"
            R.id.timeline_task_item_1 -> "task_1_completed"
            else -> null
        }

        taskKey?.let {
            requireActivity().getSharedPreferences("task_completion", Context.MODE_PRIVATE).edit {
                putBoolean(it, true)
            }
        }
    }

    private fun addAquaPoints(pointsToAdd: Int) {
        val userId = auth.currentUser?.uid ?: return
        val userDocRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userDocRef)
            val currentPoints = snapshot.getLong("aqua_points")?.toInt() ?: 0
            val newPoints = currentPoints + pointsToAdd
            transaction.update(userDocRef, "aqua_points", newPoints)

            requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).edit {
                putInt("aqua_points", newPoints)
            }
            null
        }.addOnSuccessListener {
            Toast.makeText(requireContext(), "+$pointsToAdd Aqua Points!", Toast.LENGTH_SHORT).show()
        }
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
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val photoFile: File? = try {
            File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            )
        } catch (ex: java.io.IOException) {
            null
        }

        photoFile?.also { 
            latestTmpUri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.co2_.fileprovider",
                it
            )
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, latestTmpUri)
            takePictureLauncher.launch(takePictureIntent)
        }
    }

    private fun openGallery() {
        pickVisualMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
