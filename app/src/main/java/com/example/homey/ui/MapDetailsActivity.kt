package com.example.homey.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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

class MapDetailsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var geoCoder: Geocoder
    private lateinit var currentLocation: Location
    private var lat = 0.0
    private var lon = 0.0
    private var location: String? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                getCurrentLocation { success ->
                    if (success) {
                        openGoogleMaps(lat, lon)
                    }
                }
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                getCurrentLocation { success ->
                    if (success) {
                        openGoogleMaps(lat, lon)
                    }
                }
            }

            else -> {
                // No location access granted.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        location = intent.getStringExtra("location")

        geoCoder = Geocoder(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = location

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync(this)

        // Open Google Maps when the user clicks on the map
        val button = findViewById<Button>(R.id.openGoogleMapsButton)
        button.setOnClickListener {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun getCurrentLocation(onComplete: (Boolean) -> Unit) {
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
                    currentLocation = location
                    onComplete(true)
                }
            }
            .addOnFailureListener { exception ->
                // Handle the exception
            }
    }

    private fun openGoogleMaps(lat: Double, lon: Double) {
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${currentLocation.latitude},${currentLocation.longitude}&destination=$lat,$lon&travelmode=driving")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        try {
            var addressList = mutableListOf<Address>()
            geoCoder.getFromLocationName(location!!, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (addresses.isNotEmpty()) {
                        addressList = addresses
                        val address = addressList[0]
                        lat = address.latitude
                        lon = address.longitude
                        val latLng = LatLng(lat, lon)
                        val advancedMarkerOptions = AdvancedMarkerOptions()
                        runOnUiThread {
                            googleMap.addMarker(
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
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}