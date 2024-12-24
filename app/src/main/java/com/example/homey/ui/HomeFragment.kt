package com.example.homey

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.homey.adapters.PostAdapter
import com.example.homey.data.model.Post
import com.example.homey.data.repository.EstateRepository
import com.example.homey.data.repository.UserRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class HomeFragment : Fragment(), PostAdapter.PostAdapterCallback {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private lateinit var posts : MutableList<Post>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Hide action bar
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        val estateRepo = EstateRepository.getInstance()
        val userRepository = UserRepository.getInstance()

        posts = mutableListOf<Post>()


        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        getLastLocation { success ->
            if (success) {
                estateRepo.getEstates(currentLocation.latitude, currentLocation.longitude) { estates ->
                    if (estates != null) {
                        for (estate in estates) {
                            userRepository.getAvatarAndUsernameAndPhoneNumberAndFavoriteState(estate.ownerUid, estate.id!!) { success, avatar, username, phoneNumber, isFavorite ->
                                if (success && username != null && phoneNumber != null && isFavorite != null && avatar != null) {
                                    val post = Post(
                                        estate.id!!,
                                        estate.images[0],
                                        estate.images[1],
                                        estate.images[2],
                                        estate.images[3],
                                        estate.title,
                                        estate.price,
                                        estate.size,
                                        estate.location,
                                        avatar,
                                        username,
                                        phoneNumber,
                                        estate.bedrooms,
                                        estate.bathrooms,
                                        estate.postTime,
                                        isFavorite,
                                    )
                                    posts.add(post)
                                }
                                if (posts.size == estates.size) {
                                    val listView = view.findViewById<ListView>(R.id.itemPost)
                                    val adapter = PostAdapter(requireContext(), posts, this)
                                    listView.adapter = adapter
                                    adapter.notifyDataSetChanged()
                                }

                            }
                        }
                    }
                }
            }
        }

        val listView = view.findViewById<ListView>(R.id.itemPost)
        val adapter = PostAdapter(requireContext(), posts, this)

        listView.adapter = adapter
        adapter.notifyDataSetChanged()

        return view
    }

    override fun onFavoriteButtonClicked(postId: String) {
        val fragmentManager = parentFragmentManager
        val favoriteFragment = fragmentManager.findFragmentByTag("FAVORITE_FRAGMENT")

        favoriteFragment?.let {
            fragmentManager.beginTransaction().apply {
                detach(it)
                commit()
            }
            fragmentManager.beginTransaction().apply {
                attach(it)
                commit()
            }
        }
    }

    private fun getLastLocation(onComplete: (Boolean) -> Unit) {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    getCurrentLocation() { success ->
                        onComplete(success)
                    }
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    getCurrentLocation() { success ->
                        onComplete(success)
                    }
                }

                else -> {
                    // No location access granted.
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun getCurrentLocation(onComplete: (Boolean) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
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
}