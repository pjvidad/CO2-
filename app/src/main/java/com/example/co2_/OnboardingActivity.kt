package com.example.co2_

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.co2_.databinding.OnboardingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: OnboardingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private val calendar = Calendar.getInstance()
    private var imageUri: Uri? = null

    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val croppedUri = result.uriContent
            croppedUri?.let {
                imageUri = it
                Glide.with(this).load(it).into(binding.profileImageView)
            }
        } else {
            val exception = result.error
            Toast.makeText(this, "Image cropping failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val cropOptions = CropImageContractOptions(
                uri = it, cropImageOptions = CropImageOptions(
                    fixAspectRatio = true,
                    aspectRatioX = 1,
                    aspectRatioY = 1,
                    guidelines = CropImageView.Guidelines.ON,
                    outputCompressFormat = Bitmap.CompressFormat.JPEG,
                    outputCompressQuality = 80
                )
            )
            cropImageLauncher.launch(cropOptions)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val email = intent.getStringExtra("EMAIL")
        val isGoogleSignIn = intent.getBooleanExtra("IS_GOOGLE_SIGN_IN", false)

        if (email != null) {
            binding.inputEmail.setText(email)
        }

        if (isGoogleSignIn) {
            binding.inputEmail.isEnabled = false
            binding.inputEmail.alpha = 0.5f
            loadUserDataForGoogleSignIn()
        }

        binding.userDOB.setOnClickListener {
            showDatePickerDialog()
        }

        binding.uploadPhotoButton.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        binding.createAccButton.setOnClickListener {
            if (isGoogleSignIn) {
                updateDetailsForGoogleUser()
            } else {
                createEmailPasswordUser()
            }
        }

        if (!isGoogleSignIn) {
            loadProfileImage()
        }
    }

    private fun loadUserDataForGoogleSignIn() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                if (document != null) {
                    binding.userNameInput.setText(document.getString("name"))
                    binding.userDOB.setText(document.getString("date_of_birth"))
                    val photoUrl = document.getString("profile_picture")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this).load(photoUrl).into(binding.profileImageView)
                    }
                }
            }
        }
    }


    private fun loadProfileImage() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                if (document != null) {
                    val photoUrl = document.getString("profile_picture")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this).load(photoUrl).into(binding.profileImageView)
                    }
                }
            }
        }
    }

    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateInView() {
        val myFormat = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.userDOB.setText(sdf.format(calendar.time))
    }

    private fun createEmailPasswordUser() {
        val name = binding.userNameInput.text.toString().trim()
        val dob = binding.userDOB.text.toString().trim()
        val currentEmail = binding.inputEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            TextUtils.isEmpty(name) -> Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(dob) -> Toast.makeText(this, "Date of Birth cannot be empty.", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(currentEmail) || TextUtils.isEmpty(password) -> Toast.makeText(this, "Email and password cannot be empty.", Toast.LENGTH_SHORT).show()
            !Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches() -> Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
            else -> {
                auth.createUserWithEmailAndPassword(currentEmail, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser!!
                            firebaseUser.sendEmailVerification().addOnCompleteListener { emailTask ->
                                if(emailTask.isSuccessful){
                                    saveNewUserData(firebaseUser, name, dob, currentEmail)
                                } else {
                                    Toast.makeText(this, "Failed to send verification email. Please try again.", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun saveNewUserData(firebaseUser: FirebaseUser, name: String, dob: String, email: String) {
        if (imageUri != null) {
            val storageRef = storage.reference.child("profile_pictures/${firebaseUser.uid}")
            storageRef.putFile(imageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        createNewUserInFirestore(firebaseUser, name, dob, email, uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                    createNewUserInFirestore(firebaseUser, name, dob, email, "")
                }
        } else {
            createNewUserInFirestore(firebaseUser, name, dob, email, "")
        }
    }

    private fun createNewUserInFirestore(firebaseUser: FirebaseUser, name: String, dob: String, email: String, photoUrl: String) {
        val userMap = hashMapOf(
            "name" to name,
            "date_of_birth" to dob,
            "email" to email,
            "profile_picture" to photoUrl,
            "aqua_points" to 0,
            "daily_impact" to 0.0,
            "monthly_impact" to 0.0
        )

        db.collection("users").document(firebaseUser.uid).set(userMap)
            .addOnSuccessListener {
                showVerificationDialog()
            }
            .addOnFailureListener { e ->
                Toast.makeText(baseContext, "CRITICAL: Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateDetailsForGoogleUser() {
        val name = binding.userNameInput.text.toString().trim()
        val dob = binding.userDOB.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            TextUtils.isEmpty(name) || TextUtils.isEmpty(dob) -> {
                Toast.makeText(this, "Name and Date of Birth cannot be empty.", Toast.LENGTH_SHORT).show()
                return
            }
            TextUtils.isEmpty(password) -> {
                Toast.makeText(this, "Password cannot be empty.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val firebaseUser = auth.currentUser!!
        firebaseUser.updatePassword(password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                handleGoogleUserUpdate(firebaseUser.uid, name, dob)
            } else {
                Toast.makeText(this, "Failed to set password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGoogleUserUpdate(uid: String, name: String, dob: String) {
        if (imageUri != null) {
            val storageRef = storage.reference.child("profile_pictures/$uid")
            storageRef.putFile(imageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        updateUserInFirestore(uid, name, dob, uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            updateUserInFirestore(uid, name, dob, null)
        }
    }

    private fun updateUserInFirestore(uid: String, name: String, dob: String, newPhotoUrl: String?) {
        val userUpdates = mutableMapOf<String, Any>(
            "name" to name,
            "date_of_birth" to dob
        )
        if (newPhotoUrl != null) {
            userUpdates["profile_picture"] = newPhotoUrl
        }

        db.collection("users").document(uid).update(userUpdates)
            .addOnSuccessListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(baseContext, "Failed to update user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showVerificationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Verify Your Email")
            .setMessage("A verification link has been sent to your email address. Please verify your email to continue.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .show()
    }
}
