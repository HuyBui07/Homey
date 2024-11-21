package com.example.homey.ui

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homey.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class SpecifyLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var currentMarker: Marker? = null
    private lateinit var mapSearchView: SearchView
    private lateinit var geoCoder: Geocoder
    private lateinit var addLocationButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_specify_location)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        geoCoder = Geocoder(this)

        // Enable the "Up" button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapSearchView = findViewById(R.id.searchView)
        mapSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val location = mapSearchView.query.toString()
                var addressList = mutableListOf<Address>()

                try {
                    geoCoder.getFromLocationName(location, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {

                            if (addresses.isNotEmpty()) {
                                addressList = addresses
                                val address = addressList[0]
                                val latLng = LatLng(address.latitude, address.longitude)
                                val advancedMarkerOptions = AdvancedMarkerOptions()
                                runOnUiThread {
                                    if (currentMarker != null) {
                                        currentMarker?.remove()
                                    }
                                    currentMarker = googleMap.addMarker(
                                        advancedMarkerOptions.position(latLng).title(location)
                                    )
                                    googleMap.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            latLng, 17f
                                        )
                                    )
                                }
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            super.onError(errorMessage)

                        }

                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        mapFragment.getMapAsync(this)

        addLocationButton = findViewById(R.id.addLocationButton)
        addLocationButton.setOnClickListener {
            val intent = intent
            intent.putExtra("location", mapSearchView.query.toString())
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        googleMap.setOnMapClickListener(this::onMapClick)
    }

    private fun onMapClick(latLng: LatLng) {
        val advancedMarkerOptions = AdvancedMarkerOptions()
        runOnUiThread {
            if (currentMarker != null) {
                currentMarker?.remove()
            }
            currentMarker = googleMap.addMarker(
                advancedMarkerOptions.position(latLng)
            )
        }

        try {
            geoCoder.getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {

                        if (addresses.isNotEmpty()) {
                            val address: Address = addresses[0]
                            val addressText = address.getAddressLine(0)
                            runOnUiThread {
                                mapSearchView.setQuery(addressText, false)
                            }
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        super.onError(errorMessage)

                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the "Up" button click
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}