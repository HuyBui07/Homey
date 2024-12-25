package com.example.homey.ui

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homey.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.LatLng

class MapDetailsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var geoCoder: Geocoder
    private var lat = 0.0
    private var lon = 0.0
    private var location: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
            openGoogleMaps(lat, lon)
        }
    }

    private fun openGoogleMaps(lat: Double, lon: Double) {
        val uri = Uri.parse("geo:0,0?q=$lat,$lon")
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