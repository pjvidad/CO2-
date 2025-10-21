package com.example.co2_

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Default fragment
        replaceFragment(HomeFragment())

        // Nav button listeners
        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            replaceFragment(HomeFragment())
        }

        findViewById<ImageButton>(R.id.navBook).setOnClickListener {
            replaceFragment(BookFragment())
        }

        findViewById<ImageButton>(R.id.navCalc).setOnClickListener {
            replaceFragment(TrackerFragment())
        }

        findViewById<ImageButton>(R.id.navBag).setOnClickListener {
            replaceFragment(ShopFragment())
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}

