package com.example.co2_

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.co2_.databinding.TrackerMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class TrackerMapFragment : DialogFragment(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private var _binding: TrackerMapBinding? = null
    private val binding get() = _binding!!
    private var googleMap: GoogleMap? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TrackerMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.buttonClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.setOnInfoWindowClickListener(this) // Set the listener

        val baguioCity = LatLng(16.4023, 120.5960)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(baguioCity, 13f))

        fetchLocations()
    }

    private fun fetchLocations() {
        db.collection("location_information")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val geoPoint = document.getGeoPoint("location")
                    val name = document.getString("name") ?: "No Name"
                    if (geoPoint != null) {
                        val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
                        googleMap?.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(name)
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("TrackerMapFragment", "Error getting documents: ", exception)
            }
    }

    override fun onInfoWindowClick(marker: Marker) {
        // Create a Uri from the marker's position and title to pass to the Maps app
        val gmmIntentUri = Uri.parse("geo:${marker.position.latitude},${marker.position.longitude}?q=${Uri.encode(marker.title)}")

        // Create an Intent to launch Google Maps
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        // Verify that the Google Maps app is installed and can handle this intent
        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}
