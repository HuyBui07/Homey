package com.example.homey.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.TextView
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.homey.R

class HomeFragment : Fragment(), PostAdapter.PostAdapterCallback {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private lateinit var posts : MutableList<Post>
    private lateinit var estateRepo: EstateRepository
    private lateinit var userRepository: UserRepository

    private lateinit var listView: ListView
    private lateinit var adapter: PostAdapter
    private var searchRadius = 10.0
    private var isFetching = false
    private var selectedType: String = "Tất cả"
    private var selectedPrice: String = "Tất cả"
    private var selectedSort = "Tin mới nhất"

    private lateinit var filterTypeButton: Button
    private lateinit var filterPriceButton: Button
    private lateinit var filterSortButton: Button

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

        searchRadius = 10.0

        // Hide action bar
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        estateRepo = EstateRepository.getInstance()
        userRepository = UserRepository.getInstance()

        posts = mutableListOf<Post>()


        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        getLastLocation { success ->
            if (success) {
                fetchEstates()
            }
        }

        val searchBar = view.findViewById<EditText>(R.id.searchBar)
        searchBar.isFocusable = false
        searchBar.isFocusableInTouchMode = false
        searchBar.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivityForResult(intent, 1)
        }

        adapter = PostAdapter(requireContext(), posts, this)
        listView = view.findViewById(R.id.itemPost)
        listView.adapter = adapter

        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}

            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount > 0 && searchRadius < 1700.0) {
                    Log.d("HomeFragment", "Fetching more estates at radius $searchRadius")
                    if (isFetching) return
                    searchRadius += 10.0
                    fetchEstates()
                }
            }
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedPost = posts[position]
            val intent = Intent(requireContext(), DetailEstateActivity::class.java)
            intent.putExtra("selectedPostId", selectedPost.id)
            startActivity(intent)
        }

        filterTypeButton = view.findViewById<Button>(R.id.filterType)
        filterTypeButton.setOnClickListener {
            showFilterTypeDialog()
        }

        filterPriceButton = view.findViewById<Button>(R.id.filterPrice)
        filterPriceButton.setOnClickListener {
            showFilterPriceDialog()
        }

        filterSortButton = view.findViewById<Button>(R.id.filterSort)
        filterSortButton.setOnClickListener {
            showSortDialog()
        }

        return view
    }

    private fun fetchEstates() {
        isFetching = true
        estateRepo.getEstates(currentLocation.latitude, currentLocation.longitude, searchRadius) { estates ->
            if (!estates.isNullOrEmpty()) {
                Log.d("HomeFragment", "Fetched estates at radius $searchRadius: $estates ")
                val newPosts = mutableListOf<Post>()
                var newEstatesSize = estates.size
                for (estate in estates) {
                    if (posts.none { it.id == estate.id }) {
                        userRepository.getAvatarAndUsernameAndPhoneNumberAndFavoriteState(estate.ownerUid, estate.id!!) { success, avatar, username, phoneNumber, isFavorite ->
                            if (success && username != null && phoneNumber != null && isFavorite != null && avatar != null) {
                                val post = Post(
                                    estate.id!!,
                                    estate.images[0],
                                    estate.images[1],
                                    estate.images[2],
                                    estate.images[3],
                                    estate.title,
                                    estate.propertyType,
                                    estate.price,
                                    estate.size,
                                    estate.location,
                                    avatar,
                                    username,
                                    phoneNumber,
                                    estate.bedrooms,
                                    estate.bathrooms,
                                    estate.postTime,
                                    estate.description,
                                    estate.frontage,
                                    estate.orientation,
                                    estate.legalStatus,
                                    estate.furnishings,
                                    isFavorite,
                                )
                                newPosts.add(post)
                            }
                            if (newPosts.size == newEstatesSize) {
                                activity?.runOnUiThread {
                                    posts.addAll(newPosts)
                                    adapter.notifyDataSetChanged()
                                    isFetching = false
                                }
                            }
                        }
                    } else {
                        newEstatesSize--
                    }
                }
            }
        }
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

    private fun showFilterTypeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)


        val listName = dialogView.findViewById<TextView>(R.id.filterTitle)
        listName.setText("Chọn loại nhà")

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnCloseFilter)
        val listFilterTypes = dialogView.findViewById<ListView>(R.id.listFilterTypes)

        val filterTypes = listOf("Tất cả nhà đất", "Căn hộ chung cư", "Nhà bán", "Đất bán", "Khác")
        val filterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, filterTypes)
        listFilterTypes.adapter = filterAdapter

        listFilterTypes.setOnItemClickListener { parent, view, position, id ->
            selectedType = filterTypes[position]
            filterTypeButton.text = selectedType
            applyFiltersAndSorting()
            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()
    }

    private fun showFilterPriceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)

        val listName = dialogView.findViewById<TextView>(R.id.filterTitle)
        listName.setText("Chọn giá nhà")

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnCloseFilter)
        val listFilterPrices = dialogView.findViewById<ListView>(R.id.listFilterTypes)

        val filterPrices = listOf("Tất cả mức giá", "Dưới 500 triệu", "500-800 triệu", "800 triệu - 1 tỷ", "Trên 1 tỷ")
        val filterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, filterPrices)
        listFilterPrices.adapter = filterAdapter

        listFilterPrices.setOnItemClickListener { parent, view, position, id ->
            selectedPrice = filterPrices[position]
            filterPriceButton.text = selectedPrice
            applyFiltersAndSorting()
            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()
    }

    private fun showSortDialog() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)

        val listName = dialogView.findViewById<TextView>(R.id.filterTitle)
        listName.setText("Sắp xếp theo")

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnCloseFilter)
        val listSortOptions = dialogView.findViewById<ListView>(R.id.listFilterTypes)

        val sortOptions = listOf("Thông thường", "Giá giảm", "Giá tăng", "Diện tích giảm", "Diện tích tăng")

        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, sortOptions)
        listSortOptions.adapter = sortAdapter

        listSortOptions.setOnItemClickListener { _, _, position, _ ->
            selectedSort = sortOptions[position]
            filterSortButton.text = selectedSort
            applyFiltersAndSorting()
            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()
    }

    private fun applyFiltersAndSorting() {
        val filteredPosts = posts.filter {
            val matchesType = when (selectedType) {
                "Căn hộ chung cư" -> it.propertyType == "Căn hộ chung cư"
                "Nhà bán" -> it.propertyType == "Nhà bán"
                "Đất bán" -> it.propertyType == "Đất bán"
                "Khác" -> it.propertyType== "Khác"
                else -> true
            }
            val matchesPrice = when (selectedPrice) {
                "Dưới 500 triệu" -> it.price < 500000000
                "500-800 triệu" -> it.price >= 500000000 && it.price <800000000
                "800 triệu - 1 tỷ" -> it.price >= 800000000 && it.price < 1000000000
                "Trên 1 tỷ" -> it.price >= 1000000000
                else -> true
            }
            matchesType && matchesPrice
        }

        val sortedPosts = when (selectedSort) {
            "Giá tăng" -> filteredPosts.sortedBy { it.price }
            "Giá giảm" -> filteredPosts.sortedByDescending { it.price }
            "Diện tích tăng" -> filteredPosts.sortedBy { it.area }
            "Diện tích giảm" -> filteredPosts.sortedByDescending { it.area }
            else -> filteredPosts
        }


        adapter = PostAdapter(requireContext(), sortedPosts, this)
        listView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Reload the fragment
            val fragmentManager = parentFragmentManager
            val homeFragment = fragmentManager.findFragmentByTag("HOME_FRAGMENT")
            val favoriteFragment = fragmentManager.findFragmentByTag("FAVORITE_FRAGMENT")

            homeFragment?.let {
                fragmentManager.beginTransaction().apply {
                    detach(it)
                    commit()
                }
                fragmentManager.beginTransaction().apply {
                    attach(it)
                    commit()
                }
            }

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
    }
}

