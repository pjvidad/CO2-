package com.example.co2_

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome)

        // find the button by its ID (make sure it's the same in welcome.xml)
        val btnGetStarted = findViewById<Button>(R.id.bookTitle0)

        // set click listener
        btnGetStarted.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }
}
