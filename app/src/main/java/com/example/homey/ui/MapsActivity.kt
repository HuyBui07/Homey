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
import com.example.homey.data.repository.EstateRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var geoCoder: Geocoder
    private lateinit var currentLocation: Location
    private var lat = 0.0
    private var lon = 0.0
    private lateinit var openGoogleMapsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_maps)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentLocation = intent.getParcelableExtra("currentLocation")!!
        openGoogleMapsButton = findViewById(R.id.openGoogleMapsButton)

        geoCoder = Geocoder(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = "Map"

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        googleMap.setOnMarkerClickListener { marker ->
            openGoogleMapsButton.visibility = Button.VISIBLE
            openGoogleMapsButton.setOnClickListener {
                val intent = Intent(this, DetailEstateActivity::class.java)
                val id = marker.snippet
                intent.putExtra("selectedPostId", id)
                startActivity(intent)
            }
            marker.showInfoWindow()
            true
        }
        fetchEstates()
    }

    fun addLocationsMarker() {
        EstateRepository.getInstance().getEstates(currentLocation.latitude, currentLocation.longitude, 20.0) { estates ->
            if (estates != null) {
                for (estate in estates) {
                    val latLng = LatLng(estate.lat, estate.lon)
                    val advancedMarkerOptions = AdvancedMarkerOptions()
                    runOnUiThread {
                        googleMap.addMarker(
                            advancedMarkerOptions.position(latLng).title(estate.location).snippet(estate.id)
                        )
                    }
                }
            }
        }
    }

    fun fetchEstates() {
        try {
            var addressList = mutableListOf<Address>()
            geoCoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (addresses.isNotEmpty()) {
                        addressList = addresses
                        val address = addressList[0]
                        lat = address.latitude
                        lon = address.longitude
                        val latLng = LatLng(lat, lon)
                        runOnUiThread {
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLng, 11f
                                )
                            )
                        }
                        addLocationsMarker()
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