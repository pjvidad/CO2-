package com.example.co2_

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Default fragment
        //replaceFragment(HomeFragment())

        // Nav button listeners
        //findViewById<ImageButton>(R.id.navHome).setOnClickListener {
          //  replaceFragment(HomeFragment())
        //}

        //findViewById<ImageButton>(R.id.navBook).setOnClickListener {
          //  replaceFragment(BookFragment())
        //}

        //findViewById<ImageButton>(R.id.navCalc).setOnClickListener {
         //   replaceFragment(CalcFragment())
        //}

        //findViewById<ImageButton>(R.id.navBag).setOnClickListener {
         //   replaceFragment(BagFragment())
        //}
    }

   // private fun replaceFragment(fragment: Fragment) {
     //   supportFragmentManager.beginTransaction()
         //   .replace(R.id.fragmentContainer, fragment)
       //     .commit()
    }
//}
