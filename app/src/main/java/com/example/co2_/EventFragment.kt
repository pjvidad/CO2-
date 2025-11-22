package com.example.co2_

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.co2_.databinding.HomeEventBinding
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventFragment : Fragment() {

    private var _binding: HomeEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadCachedUserData()
        loadUserData()

        binding.carouselRecyclerView.layoutManager = CarouselLayoutManager()
        CarouselSnapHelper().attachToRecyclerView(binding.carouselRecyclerView)

        val carouselData = listOf(
            Pair(R.drawable.event1, "https://www.facebook.com/share/1LYiW4uF3i/"),
            Pair(R.drawable.event2, "https://www.facebook.com/share/16Bz9EDpHC/")
        )

        val adapter = CarouselAdapter(carouselData) { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        binding.carouselRecyclerView.adapter = adapter

        binding.taskButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun loadCachedUserData() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("user_name", "User")
        val photoUrl = sharedPreferences.getString("user_photo_url", null)

        binding.username2.text = name
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

                        binding.username2.text = name
                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(this).load(photoUrl).into(binding.profileImage)
                        }

                        requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).edit {
                            putString("user_name", name)
                            putString("user_photo_url", photoUrl)
                        }
                    } 
                }
        } 
    }

    private fun navigateToWelcome() {
        if (!isAdded || activity?.isFinishing == true) {
            return
        }
        val intent = Intent(activity, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
