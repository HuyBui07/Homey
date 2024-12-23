package com.example.homey.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homey.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class SpecifyLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private val FINE_PERMISSON_CODE = 1
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var location: Location
    private lateinit var googleMap: GoogleMap
    private var currentMarker: Marker? = null
    private lateinit var mapSearchView: SearchView
    private lateinit var geoCoder: Geocoder
    private lateinit var addLocationButton: Button
    private var lat = 0.0
    private var lon = 0.0

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
                                lat = address.latitude
                                lon = address.longitude
                                val latLng = LatLng(lat, lon)
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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        addLocationButton = findViewById(R.id.addLocationButton)
        addLocationButton.setOnClickListener {
            val intent = intent
            intent.putExtra("location", mapSearchView.query.toString())
            intent.putExtra("lat", lat)
            intent.putExtra("lon", lon)
            setResult(RESULT_OK, intent)
            finish()
        }
    }



    private fun getLastLocation() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    getCurrentLocation()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    getCurrentLocation()
                }
                else -> {
                    // No location access granted.
                }
            }
        }

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }


    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
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
                                            mapSearchView.setQuery(addressText, true)
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
            }
            .addOnFailureListener { exception ->
                // Handle the exception
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