package com.example.homey.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.homey.R
import com.example.homey.adapters.PostAdapter
import com.example.homey.data.model.Post
import com.example.homey.data.repository.EstateRepository
import com.example.homey.data.repository.UserRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class SearchActivity : AppCompatActivity(), PostAdapter.PostAdapterCallback {
    private lateinit var searchInput: EditText
    private lateinit var filterSortContainer: View
    private lateinit var noResultsText: TextView
    private lateinit var listView: ListView
    private lateinit var filterTypeButton: Button
    private lateinit var filterPriceButton: Button
    private lateinit var filterSortButton: Button

    private lateinit var adapter: PostAdapter
    private val posts = mutableListOf<Post>()
    private val estateRepository = EstateRepository.getInstance()

    private var selectedType: String = "Tất cả"
    private var selectedPrice: String = "Tất cả"
    private var selectedSort = "Tin mới nhất"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setupUI()

        searchInput.setOnEditorActionListener { _, _, _ ->
            val query = normalizeInput(searchInput.text.toString().trim())
            if (query.isNotEmpty()) {
                searchEstates(query)
            }
            false
        }
    }

    private fun setupUI() {
        searchInput = findViewById(R.id.searchInput)
        filterSortContainer = findViewById(R.id.filterSortContainer)
        noResultsText = findViewById(R.id.noResultsText)
        listView = findViewById(R.id.itemPost)

        filterTypeButton = findViewById<Button>(R.id.filterType).apply {
            setOnClickListener { showFilterTypeDialog() }
        }

        filterPriceButton = findViewById<Button>(R.id.filterPrice).apply {
            setOnClickListener { showFilterPriceDialog() }
        }

        filterSortButton = findViewById<Button>(R.id.filterSort).apply {
            setOnClickListener { showSortDialog() }
        }

        adapter = PostAdapter(this, posts, this)
        listView.adapter = adapter
    }

    private fun normalizeInput(input: String): String {
        return input.lowercase(Locale.getDefault()).trim()
    }

    private fun searchEstates(query: String) {
        estateRepository.searchEstatesByLocation(query) { estates ->
            if (estates.isNotEmpty()) {
                posts.clear()
                posts.addAll(estates.map { estate ->
                    Post(
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
                        "",
                        "",
                        "",
                        estate.bedrooms,
                        estate.bathrooms,
                        estate.postTime,
                        estate.description,
                        estate.frontage,
                        estate.orientation,
                        estate.legalStatus,
                        estate.furnishings,
                        false
                    )
                })

                adapter.updatePosts(posts)
                filterSortContainer.visibility = View.VISIBLE
                listView.visibility = View.VISIBLE
                noResultsText.visibility = View.GONE
            } else {
                filterSortContainer.visibility = View.GONE
                listView.visibility = View.GONE
                noResultsText.visibility = View.VISIBLE
            }
        }
    }

    private fun showFilterTypeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)

        val listName = dialogView.findViewById<TextView>(R.id.filterTitle)
        listName.text = "Chọn loại nhà"

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnCloseFilter)
        val listFilterTypes = dialogView.findViewById<ListView>(R.id.listFilterTypes)

        val filterTypes = listOf("Tất cả nhà đất", "Căn hộ chung cư", "Nhà bán", "Đất bán", "Khác")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filterTypes)
        listFilterTypes.adapter = filterAdapter

        listFilterTypes.setOnItemClickListener { _, _, position, _ ->
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
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)

        val listName = dialogView.findViewById<TextView>(R.id.filterTitle)
        listName.text = "Chọn giá nhà"

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnCloseFilter)
        val listFilterPrices = dialogView.findViewById<ListView>(R.id.listFilterTypes)

        val filterPrices = listOf("Tất cả mức giá", "Dưới 500 triệu", "500-800 triệu", "800 triệu - 1 tỷ", "Trên 1 tỷ")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filterPrices)
        listFilterPrices.adapter = filterAdapter

        listFilterPrices.setOnItemClickListener { _, _, position, _ ->
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
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)

        val listName = dialogView.findViewById<TextView>(R.id.filterTitle)
        listName.text = "Sắp xếp theo"

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnCloseFilter)
        val listSortOptions = dialogView.findViewById<ListView>(R.id.listFilterTypes)

        val sortOptions = listOf("Thông thường", "Giá giảm", "Giá tăng", "Diện tích giảm", "Diện tích tăng")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, sortOptions)
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
        val filteredPosts = posts.filter { post ->
            val matchesType = when (selectedType) {
                "Căn hộ chung cư" -> post.propertyType == "Căn hộ chung cư"
                "Nhà bán" -> post.propertyType == "Nhà bán"
                "Đất bán" -> post.propertyType == "Đất bán"
                "Khác" -> post.propertyType == "Khác"
                else -> true
            }

            val matchesPrice = when (selectedPrice) {
                "Dưới 500 triệu" -> post.price < 500_000_000.0
                "500-800 triệu" -> post.price in 500_000_000.0..800_000_000.0
                "800 triệu - 1 tỷ" -> post.price in 800_000_000.0..1_000_000_000.0
                "Trên 1 tỷ" -> post.price > 1_000_000_000.0
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

        adapter.updatePosts(sortedPosts)
    }


    override fun onFavoriteButtonClicked(postId: String) {
        Log.d("SearchActivity", "Favorite clicked: $postId")
    }
}
