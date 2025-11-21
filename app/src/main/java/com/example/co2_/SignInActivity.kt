package com.example.co2_

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.co2_.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("SignInActivity", "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Make sure this is in your strings.xml
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/user.birthday.read"))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, PasswordResetActivity::class.java))
        }

        // Google sign-in button
        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        // Login button with email and password validation
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            when {
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password) -> {
                    Toast.makeText(this, "Email and password cannot be empty.", Toast.LENGTH_SHORT).show()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null && user.isEmailVerified) {
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(baseContext, "Please verify your email address.", Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                }
                            } else {
                                if (task.exception is FirebaseAuthInvalidUserException) {
                                    val intent = Intent(this, OnboardingActivity::class.java)
                                    intent.putExtra("EMAIL", email)
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        baseContext, "Authentication failed.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    val user = auth.currentUser!!
                    if (isNewUser) {
                        var dob = ""
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val birthdays = task.result?.additionalUserInfo?.profile?.get("birthdays") as? List<Map<String, Any>>
                            if (birthdays != null && birthdays.isNotEmpty()) {
                                @Suppress("UNCHECKED_CAST")
                                val date = birthdays[0]["date"] as? Map<String, Double>
                                if (date != null) {
                                    val year = date["year"]?.toInt()
                                    val month = date["month"]?.toInt()
                                    val day = date["day"]?.toInt()
                                    if (year != null && month != null && day != null) {
                                        dob = String.format(Locale.US, "%02d/%02d/%04d", month, day, year)
                                    }
                                }
                            }
                        } catch(e: Exception) {
                            Log.e("SignInActivity", "Error parsing birthday", e)
                        }

                        val userMap = hashMapOf(
                            "name" to user.displayName,
                            "date_of_birth" to dob,
                            "email" to user.email,
                            "profile_picture" to user.photoUrl.toString(),
                            "aqua_points" to 0
                        )
                        db.collection("users").document(user.uid).set(userMap).addOnSuccessListener {
                            val intent = Intent(this, OnboardingActivity::class.java)
                            intent.putExtra("EMAIL", user.email)
                            intent.putExtra("IS_GOOGLE_SIGN_IN", true)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
