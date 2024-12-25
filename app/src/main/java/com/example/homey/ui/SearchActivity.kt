package com.example.homey.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.homey.R
import com.example.homey.adapters.EstateAdapter
import com.example.homey.data.model.Estate
import com.example.homey.data.repository.EstateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var searchResults: RecyclerView
    private lateinit var noResultsText: TextView
    private lateinit var adapter: EstateAdapter
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private val estateRepository = EstateRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedEstateId = result.data?.getStringExtra("updatedEstateId")
                Toast.makeText(this, "Updated estate: $updatedEstateId", Toast.LENGTH_SHORT).show()
            }
        }

        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        searchResults = findViewById(R.id.searchResults)
        noResultsText = findViewById(R.id.noResultsText)

        searchResults.layoutManager = LinearLayoutManager(this)
        adapter = EstateAdapter(emptyList(), startForResult)
        searchResults.adapter = adapter

        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            } else {
                Toast.makeText(this, "Vui lòng nhập địa chỉ để tìm kiếm.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSearch(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val estates: List<Estate> = estateRepository.searchEstatesByAddress(query)
            withContext(Dispatchers.Main) {
                if (estates.isNotEmpty()) {
                    adapter.updateEstates(estates)
                    searchResults.visibility = View.VISIBLE
                    noResultsText.visibility = View.GONE
                } else {
                    adapter.updateEstates(emptyList())
                    searchResults.visibility = View.GONE
                    noResultsText.text = "Không tìm thấy kết quả phù hợp"
                    noResultsText.visibility = View.VISIBLE
                }
            }
        }
    }
}
