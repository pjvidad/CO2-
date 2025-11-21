package com.example.co2_

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.co2_.databinding.OnboardingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: OnboardingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val email = intent.getStringExtra("EMAIL")
        val isGoogleSignIn = intent.getBooleanExtra("IS_GOOGLE_SIGN_IN", false)

        if (email != null) {
            binding.inputEmail.setText(email)
            if (isGoogleSignIn) {
                binding.inputEmail.isEnabled = false
                binding.inputEmail.alpha = 0.5f
            }
        }

        binding.createAccButton.setOnClickListener {
            val name = binding.userNameInput.text.toString().trim()
            val dob = binding.userDOB.text.toString().trim()
            val currentEmail = binding.inputEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            when {
                TextUtils.isEmpty(name) -> {
                    Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(dob) -> {
                    Toast.makeText(this, "Date of Birth cannot be empty.", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(currentEmail) || TextUtils.isEmpty(password) -> {
                    Toast.makeText(this, "Email and password cannot be empty.", Toast.LENGTH_SHORT).show()
                }
                !Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches() -> {
                    Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    auth.createUserWithEmailAndPassword(currentEmail, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = auth.currentUser
                                firebaseUser?.let {
                                    val uid = it.uid
                                    val userMap = hashMapOf(
                                        "name" to name,
                                        "date_of_birth" to dob,
                                        "email" to currentEmail,
                                        "profile_picture" to "",
                                        "aqua_points" to 0
                                    )

                                    db.collection("users").document(uid)
                                        .set(userMap)
                                        .addOnSuccessListener {
                                            firebaseUser.sendEmailVerification()?.addOnCompleteListener { sendEmailTask ->
                                                if (sendEmailTask.isSuccessful) {
                                                    showVerificationDialog()
                                                } else {
                                                    Toast.makeText(baseContext, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                baseContext, "Failed to save user data: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            } else {
                                // If sign up fails, display a message to the user.
                                Toast.makeText(
                                    baseContext, "Authentication failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
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
